package org.appsugar.cluster.service.binding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.api.KeyValue;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;

/**
 * 服务调用代理接口
 * @author NewYoung
 * 2016年6月3日下午5:52:50
 */
public class ServiceInvokeHandler implements InvocationHandler {

	private String name;
	private ServiceClusterSystem system;
	private Map<Method, Integer> methodSerialization = new ConcurrentHashMap<>();
	private Map<Method, List<String>> paramNameMap;
	private Class<?> interfaceClass;

	public ServiceInvokeHandler(ServiceClusterSystem system, Class<?> interfaceClass) {
		super();
		this.name = RPCSystemUtil.getServiceName(interfaceClass);
		this.system = system;
		this.interfaceClass = interfaceClass;
		paramNameMap = Arrays.asList(interfaceClass.getMethods()).stream()
				.map(e -> new KeyValue<>(e, RPCSystemUtil.getNameList(interfaceClass, e)))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		//处理烦人的父类方法
		switch (methodName) {
		case "equals":
			return proxy == args[0];
		case "hashCode":
			return method.hashCode();
		case "toString":
			return toString();
		default:
			break;
		}
		ServiceClusterRef serviceClusterRef = system.serviceOf(name);
		if (serviceClusterRef == null) {
			throw new ServiceNotFoundException("service " + name + " not ready");
		}
		ServiceRef serviceRef = serviceClusterRef.balance();
		if (serviceRef == null) {
			throw new ServiceNotFoundException("service " + name + " not ready");
		}
		Object message = populateMethodInvokerMessage(method, args);
		return CompletableFuture.class.isAssignableFrom(method.getReturnType())
				? invokeAsync(message, serviceRef, method) : invokeSync(message, serviceRef, method);
	}

	protected CompletableFuture<Object> invokeAsync(Object message, ServiceRef serviceRef, Method method)
			throws Throwable {
		CompletableFuture<Object> future = new CompletableFuture<Object>();
		serviceRef.ask(message, result -> {
			if (result instanceof MethodInvokeOptimizingResponse) {
				MethodInvokeOptimizingResponse response = (MethodInvokeOptimizingResponse) result;
				methodSerialization.put(method, response.getSequence());
				future.complete(response.getResult());
				return;
			}
			future.complete(result);
		}, e -> future.completeExceptionally(e));
		return future;
	}

	protected Object invokeSync(Object message, ServiceRef serviceRef, Method method) throws Throwable {
		Object result = serviceRef.ask(message);
		if (result instanceof MethodInvokeOptimizingResponse) {
			MethodInvokeOptimizingResponse response = (MethodInvokeOptimizingResponse) result;
			methodSerialization.put(method, response.getSequence());
			return response.getResult();
		}
		return result;
	}

	protected Object populateMethodInvokerMessage(Method method, Object[] params) {
		Integer sequence = methodSerialization.get(method);
		//如果该方法调用还没有被优化
		if (sequence == null) {
			return new MethodInvokeMessage(paramNameMap.get(method), params);
		}
		return new MethodInvokeOptimizingMessage(sequence, params);
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
		builder.append("ServiceInvokeHandler [name=").append(name).append(", methodSerialization=")
				.append(methodSerialization).append(", interfaceClass=").append(interfaceClass).append("]");
		return builder.toString();
	}

}