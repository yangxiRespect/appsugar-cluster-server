package org.appsugar.cluster.service.akka.share;

import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;

/**
 * 本地actor共享消息
 * @author NewYoung
 * 2016年5月27日下午4:25:06
 */
public class LocalShareMessage {
	//名称
	private String name;
	//引用
	private ActorRef ref;
	//共享完成后future回调
	private CompletableFuture<Boolean> future;

	public LocalShareMessage(String name, ActorRef ref, CompletableFuture<Boolean> future) {
		super();
		this.name = name;
		this.ref = ref;
		this.future = future;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ActorRef getRef() {
		return ref;
	}

	public void setRef(ActorRef ref) {
		this.ref = ref;
	}

	public CompletableFuture<Boolean> getFuture() {
		return future;
	}

	public void setFuture(CompletableFuture<Boolean> future) {
		this.future = future;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LocalShareMessage [name=").append(name).append(", ref=").append(ref).append(", future=")
				.append(future).append("]");
		return builder.toString();
	}

}