package org.appsugar.cluster.service.binding;

/**
 * 代理后的服务
 * @author NewYoung
 * 2016年8月29日下午1:58:03
 */
public interface ProxyServer {

	/**
	 * 获取被代理的对象类
	 */
	public Class<?> getTargetClass();

	/**
	 * 获取代理对象
	 */
	public Object getObject();

	/**
	 * 获取实际对象
	 * @author NewYoung
	 * 2016年12月8日下午3:29:39
	 */
	public Object getTarget();
}
