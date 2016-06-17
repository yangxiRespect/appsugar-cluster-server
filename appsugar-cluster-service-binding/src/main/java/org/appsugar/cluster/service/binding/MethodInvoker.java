package org.appsugar.cluster.service.binding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法调用
 * @author NewYoung
 * 2016年6月3日上午3:14:24
 */
public class MethodInvoker {

	private Object target;
	private Method method;
	private Class<?>[] parameterTypes;

	public MethodInvoker(Method method, Object target) {
		this.target = target;
		this.method = method;
		this.parameterTypes = method.getParameterTypes();
	}

	/**
	 * 调用该方法 
	 */
	public Object invoke(Object[] paramArray) throws Throwable {
		try {
			return method.invoke(target, paramArray);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvoker [target=").append(target).append(", method=").append(method).append("]");
		return builder.toString();
	}

}