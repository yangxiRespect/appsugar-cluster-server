package org.appsugar.cluster.service.util;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.FutureMessage;

/**
 * 完成future帮助类
 * @author NewYoung
 * 2016年6月20日上午10:33:30
 */
public class CompletableFutureUtil {

	/**
	 * 返回一个异常完成future
	 * @author NewYoung
	 * 2017年6月7日下午1:34:05
	 */
	public static <T> CompletableFuture<T> exceptionally(Throwable ex) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(ex);
		return future;
	}

	/**
	 * 使future完成
	 */
	public static <T> void completeNormalOrThrowable(CompletableFuture<T> future, T result, Throwable e) {
		if (e != null) {
			future.completeExceptionally(e);
		} else {
			future.complete(result);
		}
	}

	/**
	 * 获取future中的值
	 * @author NewYoung
	 * 2017年5月9日下午3:14:43
	 */
	public static final <T> T getSilently(CompletableFuture<T> future) {
		try {
			return future.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 包装上下文future
	 * @author NewYoung
	 * 2017年5月9日下午3:53:57
	 */
	public static <T> CompletableFuture<T> wrapContextFuture(CompletableFuture<T> future) {
		ServiceContext context = ServiceContextUtil.context();
		if (context == null || future.isDone() || future.isCancelled()) {
			return future;
		}
		CompletableFuture<T> notifyFuture = new CompletableFuture<>();
		future.whenComplete((r, e) -> {
			ServiceContext ctx = ServiceContextUtil.context();
			if (ctx == context) {
				CompletableFutureUtil.completeNormalOrThrowable(notifyFuture, r, e);
			} else {
				context.self()
						.tell(new FutureMessage<>(r, e,
								(r1, e1) -> CompletableFutureUtil.completeNormalOrThrowable(notifyFuture, r1, e1)),
								ctx == null ? ServiceRef.NO_SENDER : ctx.sender());
			}
		});
		return notifyFuture;
	}
}
