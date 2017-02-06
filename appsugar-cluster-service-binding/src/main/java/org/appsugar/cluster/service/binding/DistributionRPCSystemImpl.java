package org.appsugar.cluster.service.binding;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceListener;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.DynamicServiceRequest;
import org.appsugar.cluster.service.domain.RepeatMessage;
import org.appsugar.cluster.service.domain.ServiceDescriptor;
import org.appsugar.cluster.service.domain.ServiceException;
import org.appsugar.cluster.service.domain.ServiceStatusMessage;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.appsugar.cluster.service.util.ServiceContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程方法调用系统实现
 * @author NewYoung
 * 2016年6月4日上午1:10:43
 */
public class DistributionRPCSystemImpl implements DistributionRPCSystem, ServiceListener {
	private static final Logger logger = LoggerFactory.getLogger(DistributionRPCSystemImpl.class);
	private ServiceClusterSystem system;
	private Map<String, ServiceRef> serviceRefs = new ConcurrentHashMap<>();
	private Set<ServiceListener> serviceListeners = new CopyOnWriteArraySet<>();
	private Map<Object, Object> proxyCache = new ConcurrentHashMap<>();
	private Map<String, Map<Class<?>, Object>> dynamicProxyCache = new ConcurrentHashMap<>();

	protected Map<String, Integer> serviceStatus = new ConcurrentHashMap<>();

	public DistributionRPCSystemImpl(ServiceClusterSystem system) {
		super();
		this.system = system;
		system.addServiceStatusListener((n, s) -> {
			String name = n.name();
			Integer oldCount = serviceStatus.get(name);
			oldCount = oldCount == null ? 0 : oldCount;
			int count = oldCount + (Status.ACTIVE.equals(s) ? 1 : -1);
			serviceStatus.put(name, count);
			//只在存在一个服务,和失去所有服务时才调用
			if (count > 1) {
				return;
			}
			notifyServiceListener(n.name(), s);
		});
		addServiceListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T serviceOf(Class<T> ic) {
		if (proxyCache.containsKey(ic)) {
			return (T) proxyCache.get(ic);
		}
		T result = createService(ic, RPCSystemUtil.getServiceName(ic));
		proxyCache.put(ic, result);
		return result;
	}

	@Override
	public <T> T serviceOfIfPresent(Class<T> ic) {
		String serviceName = RPCSystemUtil.getServiceName(ic);
		ServiceClusterRef clusterRef = system.serviceOf(serviceName);
		if (Objects.isNull(clusterRef) || clusterRef.size() == 0) {
			return null;
		}
		return serviceOf(ic);
	}

	/**
	 * 如果动态服务不可用,需要移除对应服务对象.
	 * 动态服务不可缓存RPC对象.
	 */
	@Override
	public <T> T serviceOfDynamic(Class<T> ic, String sequence) {
		return serviceOfDynamic(ic, sequence, ServiceClusterRef::leader, false);
	}

	@Override
	public <T> T serviceOfDynamicLocally(Class<T> ic, String sequence) {
		return serviceOfDynamic(ic, sequence, RPCSystemUtil::getLocalServiceRef, true);
	}

	public <T> T serviceOfDynamic(Class<T> ic, String sequence, Function<ServiceClusterRef, ServiceRef> masterFunction,
			boolean location) {
		String serviceName = RPCSystemUtil.getDynamicServiceName(ic);
		String dynamicServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(serviceName, sequence);
		Map<Class<?>, Object> serviceProxyCache = dynamicProxyCache.get(dynamicServiceName);
		if (serviceProxyCache == null) {
			serviceProxyCache = new ConcurrentHashMap<>();
			dynamicProxyCache.put(dynamicServiceName, serviceProxyCache);
		}
		T instance = (T) serviceProxyCache.get(ic);
		if (instance != null) {
			return instance;
		}
		ServiceClusterRef clusterRef = system.serviceOf(serviceName);
		if (Objects.isNull(clusterRef) || clusterRef.size() == 0) {
			throw new ServiceException("DynamicCreateService " + serviceName + " does not exist");
		}
		ServiceRef ref = masterFunction.apply(clusterRef);
		ref.ask(new DynamicServiceRequest(sequence, location));
		instance = (T) serviceProxyCache.get(ic);
		if (instance != null) {
			return instance;
		}
		instance = createService(ic, dynamicServiceName);
		serviceProxyCache.put(ic, instance);
		return instance;
	}

	@Override
	public <T> T serviceOfDynamicIfPresent(Class<T> ic, String sequence) {
		String serviceName = RPCSystemUtil.getDynamicServiceName(ic);
		String dynamicServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(serviceName, sequence);
		ServiceClusterRef clusterRef = system.serviceOf(dynamicServiceName);
		if (Objects.isNull(clusterRef) || clusterRef.size() == 0) {
			return null;
		}
		return serviceOfDynamic(ic, sequence);
	}

	@Override
	public void serviceFor(Map<Class<?>, ?> serves, String name, boolean local) {
		Service service = new RPCService(system, this, serves);
		ServiceRef serviceRef = system.serviceFor(service, name, local);
		serviceRefs.put(name, serviceRef);
		//初始化服务
		serviceRef.tell(RepeatMessage.instance, ServiceRef.NO_SENDER);
	}

	@Override
	public void serviceFor(Map<Class<?>, ?> serves, String name) {
		//只要有一个为远程服务,那么就共享该服务
		serviceFor(serves, name, false);
	}

	@Override
	public void serviceFor(ServiceDescriptor descriptor, String name) {
		try {
			serviceForAsync(descriptor, name).get();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public CompletableFuture<Void> serviceForAsync(ServiceDescriptor descriptor, String name) {
		Map<Class<?>, Object> servesMap = new HashMap<>();
		for (Object serve : descriptor.getServes()) {
			Class<?> interfaceClass = RPCSystemUtil.getServiceClass(serve);
			servesMap.put(interfaceClass, serve);
		}
		Service service = new RPCService(system, this, servesMap);
		CompletableFuture<Void> result = new CompletableFuture<>();
		//确保回调在服务上下文中执行
		RPCSystemUtil.wrapContextFuture(system.serviceForAsync(service, name, descriptor.isLocal()))
				.whenComplete((r, e) -> {
					if (Objects.nonNull(e)) {
						result.completeExceptionally(e);
						return;
					}
					serviceRefs.put(name, r);
					r.tell(RepeatMessage.instance, ServiceRef.NO_SENDER);
					result.complete(null);
				});
		return result;
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
		ServiceRef r = serviceRefs.remove(name);
		if (r == null) {
			return;
		}
		system.stop(r);
	}

	protected void notifyServiceListener(String name, Status s) {
		for (ServiceListener l : serviceListeners) {
			try {
				l.handle(name, s);
			} catch (Throwable e) {
				logger.error("notify service listener error", e);
			}
		}
	}

	@Override
	public void handle(String name, Status status) {
		ServiceStatusMessage msg = new ServiceStatusMessage(name, status);
		if (Status.INACTIVE.equals(status)) {
			//动态服务需要移除,避免某些情况下无法创建动态服务
			dynamicProxyCache.remove(name);
		}
		for (ServiceRef ref : serviceRefs.values()) {
			ref.tell(msg, ServiceRef.NO_SENDER);
		}
	}

	@Override
	public void terminate() {
		system.terminate();
		serviceRefs.clear();
		serviceListeners.clear();
		proxyCache.clear();
		serviceStatus.clear();
		dynamicProxyCache.clear();
	}

	@Override
	public void registerFactory(DynamicServiceFactory factory) {
		String name = factory.name();
		Service service = new DynamicCreatorService(factory, system, this, name);
		ServiceRef serviceRef = system.serviceFor(service, name, factory.local());
		serviceRefs.put(serviceRef.name(), serviceRef);
	}

	@SuppressWarnings("unchecked")
	protected <T> T createService(Class<T> ic, String serviceName) {
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[] { ic, DistributionServiceInvocation.class },
				new ServiceInvokeHandler(system, ic, serviceName));
	}

	@Override
	public Collection<ServiceRef> serviceRefs() {
		return Collections.unmodifiableCollection(serviceRefs.values());
	}

}