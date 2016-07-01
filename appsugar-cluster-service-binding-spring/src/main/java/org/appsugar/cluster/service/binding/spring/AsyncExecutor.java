package org.appsugar.cluster.service.binding.spring;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步调用
 * @author NewYoung
 * 2016年7月1日下午5:04:32
 */
public class AsyncExecutor {
	private ThreadPoolTaskExecutor executor;

	public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				future.complete(supplier.get());
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}
}
