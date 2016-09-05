package org.appsugar.cluster.service.util;

import java.util.concurrent.CompletableFuture;

/**
 * 完成future帮助类
 * @author NewYoung
 * 2016年6月20日上午10:33:30
 */
public class CompletableFutureUtil {

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
}
