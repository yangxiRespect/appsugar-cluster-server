package org.appsugar.cluster.service.akka.system;

/**
 * 消息处理器
 * @author NewYoung
 * 2016年5月29日下午5:43:15
 */
public interface MessageProcessor {

	/**
	 * 处理消息
	 * @param ctx 消息处理上下文
	 * @param msg 对应消息
	 */
	Object process(ProcessorContext ctx, Object msg) throws Throwable;

}