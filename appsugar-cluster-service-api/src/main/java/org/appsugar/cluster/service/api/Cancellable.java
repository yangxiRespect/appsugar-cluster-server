package org.appsugar.cluster.service.api;

/**
 * 可关闭接口
 * @author NewYoung
 * 2016年5月23日下午1:35:13
 */
public interface Cancellable {

	/**
	 * 取消执行的任务
	 */
	boolean cancel();

	/**
	 * 查看任务是否已取消
	 */
	boolean isCancelled();

}