package org.appsugar.cluster.service.domain;

import java.util.function.BiConsumer;

/**
 * 异步结果执行消息. 
 * 主要解决夸系统之间调用
 * @author NewYoung
 * 2016年6月6日下午4:07:26
 */
public class FutureMessage<T> {
	private T result;
	private Throwable throwable;
	private BiConsumer<T, Throwable> consumer;

	public FutureMessage(T result, Throwable throwable, BiConsumer<T, Throwable> consumer) {
		super();
		this.result = result;
		this.throwable = throwable;
		this.consumer = consumer;
	}

	public T getResult() {
		return result;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public BiConsumer<T, Throwable> getConsumer() {
		return consumer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FutureMessage [result=").append(result).append(", throwable=").append(throwable)
				.append(", consumer=").append(consumer).append("]");
		return builder.toString();
	}

}
