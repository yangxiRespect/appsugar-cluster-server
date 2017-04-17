package org.appsugar.cluster.service.akka.share;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Member;

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
	public CompletableFuture<Void> share(ActorRef ref, String name) {
		return share(ref, name, false);
	}

	/**
	 * 共享actor
	 * @param ref actor引用
	 * @param name actor名称
	 */
	public CompletableFuture<Void> share(ActorRef ref, String name, boolean local) {
		return shareCenter.share(ref, name, local);
	}

	/**
	 * 获取所有节点
	 * @author NewYoung
	 * 2017年4月17日上午11:07:17
	 */
	public Set<Member> members() {
		return Collections.unmodifiableSet(shareCenter.members());
	}
}