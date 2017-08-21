package org.appsugar.cluster.service.binding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.annotation.ExecuteTimeout;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.KeyValue;
import org.appsugar.cluster.service.domain.MethodInvokeMessage;
import org.appsugar.cluster.service.domain.MethodInvokeOptimizingMessage;
import org.appsugar.cluster.service.domain.MethodInvokeOptimizingResponse;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务调用代理接口
 * @author NewYoung
 * 2016年6月3日下午5:52:50
 */
public class ServiceInvokeHandler implements InvocationHandler {
	private static final Logger logger = LoggerFactory.getLogger(ServiceInvokeHandler.class);
	public static final String METHOD_CACHE_KEY = "fast_method_cache";
	private String name;
	private ServiceClusterSystem system;
	private Map<Method, List<String>> paramNameMap;
	private Class<?> interfaceClass;
	private ServiceNotFoundException exception;

	public ServiceInvokeHandler(ServiceClusterSystem system, Class<?> interfaceClass) {
		this(system, interfaceClass, RPCSystemUtil.getServiceName(interfaceClass));
	}

	public ServiceInvokeHandler(ServiceClusterSystem system, Class<?> interfaceClass, String name) {
		super();
		this.name = name;
		this.system = system;
		this.interfaceClass = interfaceClass;
		paramNameMap = Arrays.asList(interfaceClass.getMethods()).stream()
				.map(e -> new KeyValue<>(e, RPCSystemUtil.getNameList(interfaceClass, e)))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		exception = new ServiceNotFoundException("service " + name + " not ready");

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		//处理烦人的父类方法
		switch (methodName) {
		case "equals":
			return proxy == args[0];
		case "hashCode":
			return hashCode();
		case "toString":
			return toString();
		default:
			break;
		}
		boolean async = CompletionStage.class.isAssignableFrom(method.getReturnType());
		if (!async) {
			throw new UnsupportedOperationException("sync method invoke not allowed");
		}
		ServiceClusterRef serviceClusterRef = system.serviceOf(name);
		if (serviceClusterRef == null || serviceClusterRef.size() == 0) {
			return CompletableFutureUtil.exceptionally(exception);
		}
		ServiceRef serviceRef = serviceClusterRef.balance();
		Object message = populateMethodInvokerMessage(method, args, serviceRef);
		CompletableFuture<?> future = invokeAsync(message, serviceRef, method).whenComplete((r, e) -> {
			if (Objects.nonNull(e)) {
				logger.error("invoke {}.{} failure see below exception", interfaceClass, methodName);
			}
		});
		return async ? future : future.get();
	}

	protected CompletableFuture<Object> invokeAsync(Object message, ServiceRef serviceRef, Method method)
			throws Throwable {
		long timeout = 30000;
		ExecuteTimeout timeOutAnnotation = method.getAnnotation(ExecuteTimeout.class);
		if (timeOutAnnotation != null) {
			timeout = timeOutAnnotation.value();
		}
		return serviceRef.ask(message, timeout).thenApply(result -> {
			if (result instanceof MethodInvokeOptimizingResponse) {
				MethodInvokeOptimizingResponse response = (MethodInvokeOptimizingResponse) result;
				optimizeMethodInvoker(method, response.getSequence(), serviceRef);
				return response.getResult();
			}
			return result;
		});

	}

	protected Object populateMethodInvokerMessage(Method method, Object[] params, ServiceRef ref) {
		Map<Method, Integer> sequenceMap = ref.get(METHOD_CACHE_KEY);
		if (sequenceMap == null || sequenceMap.get(method) == null) {
			return new MethodInvokeMessage(paramNameMap.get(method), params);
		}
		//根据ServiceRef获取对应sequence
		Integer sequence = sequenceMap.get(method);
		return new MethodInvokeOptimizingMessage(sequence, params);
	}

	protected void optimizeMethodInvoker(Method method, Integer sequence, ServiceRef ref) {
		//使用乐观锁
		Map<Method, Integer> sequenceMap = ref.get(METHOD_CACHE_KEY);
		if (sequenceMap == null) {
			synchronized (ref) {
				sequenceMap = ref.get(METHOD_CACHE_KEY);
				if (sequenceMap == null) {
					sequenceMap = new ConcurrentHashMap<>();
					ref.attach(METHOD_CACHE_KEY, sequenceMap);
				}
			}
		}
		sequenceMap.put(method, sequence);
	}

	public String getName() {
		return name;
	}

	public ServiceClusterSystem getSystem() {
		return system;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceInvokeHandler [name=").append(name).append(", paramNameMap=").append(paramNameMap)
				.append(", interfaceClass=").append(interfaceClass).append("]");
		return builder.toString();
	}
}