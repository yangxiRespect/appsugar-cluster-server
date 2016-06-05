package org.appsugar.cluster.service.binding;

import java.lang.reflect.Method;

/**
 * 方法调用
 * @author NewYoung
 * 2016年6月3日上午3:14:24
 */
public class MethodInvoker {

	private Object target;
	private Method method;

	public MethodInvoker(Method method, Object target) {
		this.target = target;
		this.method = method;
	}

	/**
	 * 调用该方法 
	 */
	public Object invoke(Object[] paramArray) throws Exception {
		return method.invoke(target, paramArray);
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvoker [target=").append(target).append(", method=").append(method).append("]");
		return builder.toString();
	}

}