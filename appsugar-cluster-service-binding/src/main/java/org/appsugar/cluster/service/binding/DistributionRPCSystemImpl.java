package org.appsugar.cluster.service.binding;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;
import org.appsugar.cluster.service.api.ServiceException;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.api.Status;
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
	private Map<ServiceRef, ServiceRef> serviceRefs = new ConcurrentHashMap<>();
	private Set<ServiceListener> serviceListeners = new CopyOnWriteArraySet<>();
	private Map<Object, Object> proxyCache = new ConcurrentHashMap<>();
	private Map<Object, Map<String, Object>> dynamicProxyCache = new ConcurrentHashMap<>();
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
		T result = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { ic },
				new ServiceInvokeHandler(system, ic));
		proxyCache.put(ic, result);
		return result;
	}

	@Override
	public <T> T serviceOfDynamic(Class<T> ic, String sequence) {
		String serviceName = RPCSystemUtil.getDynamicServiceName(ic);
		Map<String, Object> serviceProxyCache = dynamicProxyCache.get(ic);
		if (serviceProxyCache == null) {
			serviceProxyCache = new ConcurrentHashMap<>();
			dynamicProxyCache.put(ic, serviceProxyCache);
		}
		T instance = (T) serviceProxyCache.get(sequence);
		if (instance != null) {
			return instance;
		}
		ServiceClusterRef clusterRef = system.serviceOf(serviceName);
		if (clusterRef == null || clusterRef.size() == 0) {
			throw new ServiceException("DynamicCreateService " + serviceName + " does not exist");
		}
		ServiceRef ref = clusterRef.min();
		ref.ask(new DynamicServiceRequest(sequence));
		instance = (T) serviceProxyCache.get(sequence);
		if (instance != null) {
			return instance;
		}
		String dynamicServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(serviceName, sequence);
		instance = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { ic },
				new ServiceInvokeHandler(system, ic, dynamicServiceName));
		serviceProxyCache.put(sequence, instance);
		return instance;
	}

	@Override
	public void serviceFor(Map<Class<?>, ?> serves, String name) {
		Service service = new RPCService(system, this, serves);
		ServiceRef serviceRef = system.serviceFor(service, name);
		serviceRefs.put(serviceRef, serviceRef);
		serviceRef.tell(RepeatMessage.instance, ServiceRef.NO_SENDER);
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
		ServiceContext context = ServiceContextThreadLocal.context();
		ServiceRef ref = null;
		if (context != null) {
			ref = context.self();
		}
		system.publish(topic, msg, ref);
	}

	@Override
	public void stop(ServiceRef ref) {
		ServiceRef r = serviceRefs.remove(ref);
		if (r == null) {
			return;
		}
		system.stop(ref);
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
		Service service = new DynamicCreatorService(factory, system, factory.service());
		system.serviceFor(service, factory.service());
	}

}