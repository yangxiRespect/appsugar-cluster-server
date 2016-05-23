package org.appsugar.cluster.service.api;

/**
 * 服务接口,处理来自系统和其他服务发送过来的消息
 * 可以注册至 {@link ServiceClusterSystem#serviceFor(Service, String)}
 * @author NewYoung
 * 2016年5月23日下午1:37:57
 */
public interface Service {

	/**
	 * 处理消息
	 * @param msg 消息
	 * @param context 服务上下文
	 */
	Object handle(Object msg, ServiceContext context);

}