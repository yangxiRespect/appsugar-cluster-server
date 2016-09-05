package org.appsugar.cluster.service.binding;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.util.RPCSystemUtil;

/**
 * docker可执行接口
 * @author NewYoung
 * 2016年6月23日下午2:53:43
 * @param <R>
 * @param <P>
 */
public interface DockerExecutable<R, P> {

	/**
	 * 执行并返回 可完成future
	 * 确保completableFuture consumer执行在 service中
	 * {@link RPCSystemUtil#wrapContextFuture(CompletableFuture)}
	 */
	CompletableFuture<R> execute(P param) throws Throwable;

}