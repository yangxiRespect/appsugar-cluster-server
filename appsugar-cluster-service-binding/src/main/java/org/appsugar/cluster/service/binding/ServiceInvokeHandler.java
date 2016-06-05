package org.appsugar.cluster.service.binding;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
	private Map<Method, List<Integer>> methodSerialization;
	private Class<?> interfaceClass;
	private int classNameHashCode;

	public ServiceInvokeHandler(ServiceClusterSystem system, Class<?> interfaceClass) {
		super();
		this.name = RPCSystemUtil.getServiceName(interfaceClass);
		this.system = system;
		this.interfaceClass = interfaceClass;
		methodSerialization = RPCSystemUtil.getClassMethod(interfaceClass).entrySet().stream()
				.collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));
		this.classNameHashCode = interfaceClass.getName().hashCode();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		//处理烦人的父类方法
		switch (methodName) {
		case "equals":
			return proxy == args[0];
		case "hashCode":
			return classNameHashCode;
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
		List<Integer> serialization = methodSerialization.get(method);
		MethodInvokeMessage message = new MethodInvokeMessage(classNameHashCode, serialization, args);
		return CompletableFuture.class.isAssignableFrom(method.getReturnType()) ? invokeAsync(message, serviceRef)
				: invokeSync(message, serviceRef);
	}

	protected CompletableFuture<Object> invokeAsync(MethodInvokeMessage message, ServiceRef serviceRef)
			throws Throwable {
		CompletableFuture<Object> future = new CompletableFuture<Object>();
		serviceRef.ask(message, r -> future.complete(r), e -> future.completeExceptionally(e));
		return future;
	}

	protected Object invokeSync(MethodInvokeMessage message, ServiceRef serviceRef) throws Throwable {
		return serviceRef.ask(message);
	}

	public String getName() {
		return name;
	}

	public ServiceClusterSystem getSystem() {
		return system;
	}

	public Map<Method, List<Integer>> getMethodSerialization() {
		return methodSerialization;
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public int getClassNameHashCode() {
		return classNameHashCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceInvokeHandler [name=").append(name).append(", methodSerialization=")
				.append(methodSerialization).append(", interfaceClass=").append(interfaceClass)
				.append(", classNameHashCode=").append(classNameHashCode).append("]");
		return builder.toString();
	}

}