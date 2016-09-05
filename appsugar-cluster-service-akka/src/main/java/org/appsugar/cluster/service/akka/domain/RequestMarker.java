package org.appsugar.cluster.service.akka.domain;

import java.util.concurrent.CompletableFuture;

/**
 * 请求标记
 * @author NewYoung
 * 2016年5月29日下午4:19:36
 */
public class RequestMarker<T> {

	private int sequence;
	//请求开始时间
	private long startTime;
	//超时时间点
	private long endTime;
	//回调future
	private CompletableFuture<T> future;

	public RequestMarker(int sequence, long startTime, long endTime, CompletableFuture<T> future) {
		super();
		this.sequence = sequence;
		this.startTime = startTime;
		this.endTime = endTime;
		this.future = future;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public CompletableFuture<T> getFuture() {
		return future;
	}

	public void setFuture(CompletableFuture<T> future) {
		this.future = future;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RequestMarker [sequence=").append(sequence).append(", startTime=").append(startTime)
				.append(", endTime=").append(endTime).append(", future=").append(future).append("]");
		return builder.toString();
	}

}