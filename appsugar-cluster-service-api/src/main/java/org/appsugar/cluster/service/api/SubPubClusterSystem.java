package org.appsugar.cluster.service.api;

/**
 * 关注与发布集群系统
 * @author NewYoung
 * 2016年5月23日下午2:26:39
 */
public interface SubPubClusterSystem {

	/**
	 * 使一个服务关注一个topic 
	 * @param ref 感兴趣的服务引用
	 * @param topic 主题
	 */
	void subscribe(String topic, ServiceRef ref);

	/**
	 * 对一个主题发布消息,所有关注该主题的服务都将会收到消息
	 * @param topic 主题
	 * @param msg 消息会被封装只SubscribeEvent里
	 * @param sender 发送者
	 */
	void publish(String topic, Object msg, ServiceRef sender);

}