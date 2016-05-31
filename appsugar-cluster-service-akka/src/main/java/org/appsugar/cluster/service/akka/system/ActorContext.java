package org.appsugar.cluster.service.akka.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * actor上下文
 * @author NewYoung
 * 2016年5月29日下午5:40:42
 */
public class ActorContext {

	private ActorRef self;
	private ActorSystem system;
	private ActorRef sender;

	public ActorContext(ActorRef self, ActorSystem system) {
		super();
		this.self = self;
		this.system = system;
	}

	/**
	 * 获取自己引用
	 */
	public ActorRef self() {
		return self;
	}

	/**
	 * 获取发送者 
	 */
	public ActorRef sender() {
		return sender;
	}

	/**
	 * 获取系统
	 */
	public ActorSystem system() {
		return system;
	}

	/**
	 * 设置当前发送者
	 */
	public void sender(ActorRef s) {
		this.sender = s;
	}

}