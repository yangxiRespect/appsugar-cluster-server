package org.appsugar.cluster.service.binding;

import static org.appsugar.cluster.service.util.CompletableFutureUtil.exceptionally;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import org.appsugar.cluster.service.akka.util.DynamicServiceUtils;
import org.appsugar.cluster.service.annotation.DynamicService;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceListener;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.CommandMessage;
import org.appsugar.cluster.service.domain.DynamicServiceRequest;
import org.appsugar.cluster.service.domain.RepeatMessage;
import org.appsugar.cluster.service.domain.ServiceDescriptor;
import org.appsugar.cluster.service.domain.ServiceException;
import org.appsugar.cluster.service.domain.ServiceStatusMessage;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.appsugar.cluster.service.util.ServiceContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程方法调用系统实现
 * @author NewYoung
 * 2016年6月4日上午1:10:43
 */
public class DistributionRPCSystemImpl implements DistributionRPCSystem {
	private static final Logger logger = LoggerFactory.getLogger(DistributionRPCSystemImpl.class);
	protected ServiceClusterSystem system;
	private Set<ServiceListener> serviceListeners = new CopyOnWriteArraySet<>();
	private Map<Object, Object> proxyCache = new ConcurrentHashMap<>();
	private Map<String, Map<Class<?>, Object>> dynamicProxyCache = new ConcurrentHashMap<>();
	/**本地服务引用**/
	protected Set<ServiceRef> serviceRefs = ConcurrentHashMap.newKeySet();
	/**询问过的动态服务**/
	protected Set<String> askedDynamicService = ConcurrentHashMap.newKeySet();

	protected Map<String, List<ServiceRef>> serviceStatusFocusMap = new ConcurrentHashMap<>();

	public DistributionRPCSystemImpl(ServiceClusterSystem system) {
		super();
		this.system = system;
		system.addServiceStatusListener(this::handleServiceStatus);
	}

	void handleServiceStatus(List<ServiceRef> refs, Status status) {
		List<ServiceStatusMessage> msgs = new ArrayList<>(refs.size());
		for (ServiceRef ref : refs) {
			ServiceStatusMessage msg = new ServiceStatusMessage(ref, status);
			if (Status.INACTIVE.equals(status)) {
				//动态服务需要移除,避免某些情况下无法创建动态服务
				dynamicProxyCache.remove(ref.name());
			}
			if (ref.hasLocalScope()) {
				if (Status.ACTIVE.equals(status)) {
					serviceRefs.add(ref);
				} else {
					serviceRefs.remove(ref);
				}
			}
			msgs.add(msg);
			notifyServiceListener(ref, status);
		}
		for (ServiceStatusMessage msg : msgs) {
			String name = msg.getServiceRef().name();
			String firstName = DynamicServiceUtils.getDynamicServiceFirstName(name);
			if (!Objects.isNull(firstName)) {
				name = firstName;
			}
			List<ServiceRef> focusRefs = getServiceRefFocusOn(name);
			if (Objects.isNull(focusRefs)) {
				continue;
			}
			focusRefs.forEach(e -> e.tell(msg, ServiceRef.NO_SENDER));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T serviceOf(Class<T> ic) {
		if (proxyCache.containsKey(ic)) {
			return (T) proxyCache.get(ic);
		}
		String name = RPCSystemUtil.getServiceName(ic);
		//告知系统,我对该服务感兴趣.
		require(name);
		T result = createService(ic, name);
		proxyCache.put(ic, result);
		return result;
	}

	@Override
	public <T> CompletableFuture<T> serviceOfDynamicIfPresent(Class<T> ic, String sequence) {
		String serviceName = RPCSystemUtil.getDynamicServiceName(ic);
		String dynamicServiceName = DynamicServiceUtils.getDynamicServiceNameWithSequence(serviceName, sequence);
		//如果服务存在,那么直接返回
		if (exist(dynamicServiceName)) {
			return serviceOfDynamic(ic, sequence, ServiceClusterRef::leader, false);
		}
		//如果服务不存在,又主动询问过该服务.那么直接返回
		if (askedDynamicService.contains(dynamicServiceName)) {
			return CompletableFuture.completedFuture(null);
		}
		//如果创建者不存在,那么直接抛出异常
		if (!exist(serviceName)) {
			return exceptionally(new ServiceException("DynamicCreateService " + serviceName + " does not exist"));
		}
		//标注我所关注该动态服务
		system.focusDynamicService(serviceName, sequence);
		//寻求服务提供方查找对应动态服务
		return system.serviceOf(serviceName).leader()
				.ask(new CommandMessage(CommandMessage.QUERY_DYNAMIC_SERVICE_COMMAND, sequence))
				.whenComplete((r, e) -> askedDynamicService.add(dynamicServiceName)).thenApply(e -> {
					if (!Objects.equals(Boolean.TRUE, e)) {
						return null;
					}
					Map<Class<?>, Object> serviceProxyCache = getDynamicServiceCache(dynamicServiceName);
					T service = createService(ic, dynamicServiceName);
					serviceProxyCache.put(ic, service);
					return service;
				});
	}

	/**
	 * 如果动态服务不可用,需要移除对应服务对象.
	 * 动态服务不可缓存RPC对象.
	 */
	@Override
	public <T> CompletableFuture<T> serviceOfDynamic(Class<T> ic, String sequence) {
		return serviceOfDynamic(ic, sequence, ServiceClusterRef::leader, false);
	}

	@Override
	public <T> CompletableFuture<T> serviceOfDynamicLocally(Class<T> ic, String sequence) {
		return serviceOfDynamic(ic, sequence, RPCSystemUtil::getLocalServiceRef, true);
	}

	@SuppressWarnings("unchecked")
	public <T> CompletableFuture<T> serviceOfDynamic(Class<T> ic, String sequence,
			Function<ServiceClusterRef, ServiceRef> masterFunction, boolean location) {
		String serviceName = RPCSystemUtil.getDynamicServiceName(ic);
		String dynamicServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(serviceName, sequence);
		Map<Class<?>, Object> serviceProxyCache = getDynamicServiceCache(dynamicServiceName);
		T instance = (T) serviceProxyCache.get(ic);
		if (instance != null) {
			return CompletableFuture.completedFuture(instance);
		}
		if (exist(dynamicServiceName)) {
			instance = createService(ic, dynamicServiceName);
			serviceProxyCache.put(ic, instance);
			return CompletableFuture.completedFuture(instance);
		}
		//通知系统关注动态服务
		system.focusDynamicService(serviceName, sequence);
		if (!exist(serviceName)) {
			return exceptionally(new ServiceException("DynamicCreateService " + serviceName + " does not exist"));
		}
		Map<Class<?>, Object> finalServiceProxyCache = serviceProxyCache;
		ServiceRef ref = masterFunction.apply(system.serviceOf(serviceName));
		return ref.ask(new DynamicServiceRequest(sequence, location)).thenApply(e -> {
			T result = (T) finalServiceProxyCache.get(ic);
			if (result != null) {
				return result;
			}
			result = createService(ic, dynamicServiceName);
			finalServiceProxyCache.put(ic, result);
			return result;
		});
	}

	@Override
	public void serviceFor(ServiceDescriptor descriptor, String name) {
		CompletableFutureUtil.getSilently(serviceForAsync(descriptor, name));
	}

	@Override
	public CompletableFuture<Void> serviceForAsync(ServiceDescriptor descriptor, String name) {
		Map<Class<?>, Object> servesMap = new HashMap<>();
		for (Object serve : descriptor.getServes()) {
			Class<?> interfaceClass = RPCSystemUtil.getServiceClass(serve);
			servesMap.put(interfaceClass, serve);
		}
		RPCService service = new RPCService(system, this, servesMap);
		return RPCSystemUtil.wrapContextFuture(system.serviceForAsync(service, name, descriptor.isLocal()))
				.thenApply(r -> {
					service.self = r;
					r.tell(RepeatMessage.instance, ServiceRef.NO_SENDER);
					return null;
				});
	}

	@Override
	public boolean addServiceListener(ServiceListener listener) {
		return serviceListeners.add(listener);
	}

	@Override
	public boolean removeServiceListener(ServiceListener listener) {
		return serviceListeners.remove(listener);
	}

	@Override
	public void publish(Object msg, String topic) {
		ServiceContext context = ServiceContextUtil.context();
		ServiceRef ref = null;
		if (context != null) {
			ref = context.self();
		}
		system.publish(topic, msg, ref);
	}

	@Override
	public void stop(String name) {
		//关闭所有本地名称为name的服务
		system.serviceOf(name).iterable().forEach(e -> {
			if (!e.hasLocalScope()) {
				return;
			}
			e.tell(CommandMessage.CLOSE, ServiceRef.NO_SENDER);
		});
	}

	protected void notifyServiceListener(ServiceRef ref, Status s) {
		for (ServiceListener l : serviceListeners) {
			try {
				l.handle(ref, s);
			} catch (Throwable e) {
				logger.error("notify service lisserviceRefstener error", e);
			}
		}
	}

	@Override
	public void terminate() {
		system.terminate();
		serviceRefs.clear();
		serviceListeners.clear();
		proxyCache.clear();
		dynamicProxyCache.clear();
	}

	@Override
	public void registerFactory(DynamicServiceFactory factory) {
		String name = factory.name();
		Service service = new DynamicCreatorService(factory, system, this, name);
		ServiceRef ref = system.serviceFor(service, name, factory.local());
		//动态服务，关注动态服务事件
		getServiceRefAndCreateFocusOn(name).add(ref);
		factory.init(this);
		system.focusNormalService(name);
		system.focusSpecial(name);
	}

	@SuppressWarnings("unchecked")
	protected <T> T createService(Class<T> ic, String serviceName) {
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[] { ic, DistributionServiceInvocation.class },
				new ServiceInvokeHandler(system, ic, serviceName));
	}

	@Override
	public Collection<ServiceRef> serviceRefs() {
		return Collections.unmodifiableCollection(serviceRefs);
	}

	@Override
	public <T> void stop(T service) {
		ServiceInvokeHandler handler = (ServiceInvokeHandler) Proxy.getInvocationHandler(service);
		String name = handler.getName();
		stop(name);
	}

	@Override
	public boolean exist(String name) {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		if (Objects.nonNull(clusterRef) && clusterRef.size() != 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean existLocally(String name) {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		return Objects.nonNull(clusterRef) && Objects.nonNull(RPCSystemUtil.getLocalServiceRef(clusterRef));
	}

	@Override
	public void require(String name) {
		system.focusNormalService(name);
	}

	@Override
	public void require(Class<?> clazz) {
		org.appsugar.cluster.service.annotation.Service s = clazz
				.getAnnotation(org.appsugar.cluster.service.annotation.Service.class);
		DynamicService ds = clazz.getAnnotation(DynamicService.class);
		if (Objects.nonNull(s)) {
			require(s.value());
		} else if (Objects.nonNull(ds)) {
			require(ds.value());
		} else {
			throw new RuntimeException("class" + clazz + " is a not annotated service "
					+ org.appsugar.cluster.service.annotation.Service.class + " or dynamic service "
					+ DynamicService.class);
		}
	}

	Map<Class<?>, Object> getDynamicServiceCache(String dynamicServiceName) {
		Map<Class<?>, Object> serviceProxyCache = dynamicProxyCache.get(dynamicServiceName);
		if (serviceProxyCache == null) {
			serviceProxyCache = new ConcurrentHashMap<>();
			dynamicProxyCache.putIfAbsent(dynamicServiceName, serviceProxyCache);
		}
		return serviceProxyCache;
	}

	List<ServiceRef> getServiceRefFocusOn(String name) {
		return serviceStatusFocusMap.get(name);
	}

	List<ServiceRef> getServiceRefAndCreateFocusOn(String name) {
		List<ServiceRef> result = serviceStatusFocusMap.get(name);
		if (result == null) {
			serviceStatusFocusMap.putIfAbsent(name, new CopyOnWriteArrayList<>());
			result = serviceStatusFocusMap.get(name);
		}
		return result;
	}
}