package org.appsugar.cluster.service.akka.share;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * actor共享系统
 * @author NewYoung
 * 2016年5月27日下午4:28:12
 */
public class ActorShareSystem {

	private static Map<ActorSystem, ActorShareSystem> systemMap = new HashMap<>();

	private ActorShareCenter shareCenter;

	public ActorShareSystem(ActorShareCenter shareCenter) {
		super();
		this.shareCenter = shareCenter;
	}

	/**
	 * 根据actor系统获取共享系统
	 * @param system 不允许为空
	 * @param listener 可以为空.
	 * @return 
	 */
	public synchronized static ActorShareSystem getSystem(ActorSystem system, ActorShareListener listener) {
		ActorShareSystem result = systemMap.get(system);
		if (result != null) {
			return result;
		}
		result = new ActorShareSystem(new ActorShareCenter(system, listener));
		systemMap.put(system, result);
		system.registerOnTermination(() -> systemMap.remove(system));
		return result;
	}

	/**
	 * 共享actor
	 * @param ref actor引用
	 * @param name actor名称
	 */
	public CompletableFuture<Boolean> share(ActorRef ref, String name) {
		return shareCenter.share(ref, name);
	}

}