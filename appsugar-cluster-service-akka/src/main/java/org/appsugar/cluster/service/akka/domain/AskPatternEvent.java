package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import akka.actor.ActorRef;

/**
 * 请求事件
 * 让一个服务去发起一个请求
 * @author NewYoung
 * 2016年5月29日下午5:04:10
 */
public class AskPatternEvent<T> implements Serializable {

	private static final long serialVersionUID = 1541004277795240706L;
	private final Object msg;
	private final CompletableFuture<T> future;
	private final long timeout;
	private final ActorRef destination;

	public AskPatternEvent(Object msg, CompletableFuture<T> future, long timeout, ActorRef destination) {
		super();
		this.msg = msg;
		this.future = future;
		this.timeout = timeout;
		this.destination = destination;
	}

	public Object getMsg() {
		return msg;
	}

	public CompletableFuture<T> getFuture() {
		return future;
	}

	public long getTimeout() {
		return timeout;
	}

	public ActorRef getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AskPatternEvent [msg=").append(msg).append(", future=").append(future).append(", timeout=")
				.append(timeout).append(", destination=").append(destination).append("]");
		return builder.toString();
	}

}