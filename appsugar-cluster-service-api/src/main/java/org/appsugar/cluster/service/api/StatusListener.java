package org.appsugar.cluster.service.api;

import org.appsugar.cluster.service.domain.Status;

/**
 * 状态监听器
 * @author NewYoung
 * 2017年5月24日下午1:33:44
 * @param <T>
 */
public interface StatusListener<T> {
	/**
	 * 状态发生变化时调用
	 * @author NewYoung
	 * 2017年5月24日下午1:34:38
	 */
	void handle(T t, Status status);
}
