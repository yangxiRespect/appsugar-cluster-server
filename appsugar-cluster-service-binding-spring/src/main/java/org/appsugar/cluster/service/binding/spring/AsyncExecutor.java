package org.appsugar.cluster.service.binding.spring;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步调用 
 * @author NewYoung
 * 2016年7月1日下午5:04:32
 */
public class AsyncExecutor {

	private ThreadPoolTaskExecutor executor;

	private TransactionalExecutor transactionalExecutor;

	/**
	 * 异步执行
	 */
	public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				future.complete(supplier.get());
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return RPCSystemUtil.wrapContextFuture(future);
	}

	/**
	 * 确保supplier方法在事物中执行
	 * @author NewYoung
	 * 2017年3月10日下午1:32:25
	 */
	public <T> CompletableFuture<T> executeInTransaction(Supplier<T> supplier) {
		return executeInTransaction(supplier, true);
	}

	/**
	 * 异步执行,保证supplier在事物中
	 * @param supplier
	 * @return
	 */
	public <T> CompletableFuture<T> executeInTransaction(Supplier<T> supplier, boolean readOnly) {
		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				T result = readOnly ? transactionalExecutor.readOnly(supplier)
						: transactionalExecutor.required(supplier);
				future.complete(result);
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return RPCSystemUtil.wrapContextFuture(future);
	}

	public void setExecutor(ThreadPoolTaskExecutor executor) {
		this.executor = executor;
	}

	public void setTransactionalExecutor(TransactionalExecutor transactionalExecutor) {
		this.transactionalExecutor = transactionalExecutor;
	}

}
