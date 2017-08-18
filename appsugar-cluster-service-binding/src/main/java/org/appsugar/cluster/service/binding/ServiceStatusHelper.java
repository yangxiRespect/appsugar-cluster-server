package org.appsugar.cluster.service.binding;

import java.lang.reflect.Method;

import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.domain.Status;

/**
 * 服务完成方法执行帮助
 * @author NewYoung
 * 2016年6月3日上午6:45:14
 */
public class ServiceStatusHelper {

	private MethodInvoker methodInvoker;

	private Class<?> interfaceClass;

	public ServiceStatusHelper(Class<?> interfaceClass, Method method, Object target) {
		super();
		this.interfaceClass = interfaceClass;
		methodInvoker = new MethodInvoker(method, target, true);
	}

	/**
	 * 如果服务完全满足,那么调用该方法
	 * 调用方法后返回true,其他返回false
	 */
	public void tryInvoke(DistributionRPCSystem system, Status s) throws Throwable {
		methodInvoker.invoke(new Object[] { system.serviceOf(interfaceClass), s });
	}

	public Class<?> getInterfaceClass() {
		return interfaceClass;
	}

	public Method getMethod() {
		return methodInvoker.getMethod();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceReadyHelper [methodInvoker=").append(methodInvoker).append(", interfaceClass=")
				.append(interfaceClass).append("]");
		return builder.toString();
	}

}