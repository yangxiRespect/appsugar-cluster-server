package org.appsugar.cluster.service.akka.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * actor处理上下文
 * @author NewYoung
 * 2016年5月29日下午5:36:49
 */
public interface ProcessorContext {

	/**
	 * 获取自己引用
	 */
	ActorRef getSelf();

	/**
	 * 获取发送者引用
	 */
	ActorRef getSender();

	/**
	 * 获取系统
	 */
	ActorSystem getSystem();

	/**
	 * 交由下一个处理器处理
	 */
	Object processNext(Object msg) throws Throwable;
}