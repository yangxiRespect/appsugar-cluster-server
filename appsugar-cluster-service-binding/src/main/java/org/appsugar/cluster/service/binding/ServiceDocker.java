package org.appsugar.cluster.service.binding;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务docker
 * 解决同一请求引起多次数据请求问题
 * @author NewYoung
 * 2016年6月23日下午2:22:40
 * @param <R>
 * @param <P>
 */
public class ServiceDocker<R, P> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDocker.class);
	private DockerExecutable<R, P> executeable;
	private Function<P, Object> sequenceFuntion;
	private Map<Object, List<CompletableFuture<R>>> dockerList = new TreeMap<>();
	private Supplier<List<CompletableFuture<R>>> linkedListSupply = LinkedList::new;

	public ServiceDocker(DockerExecutable<R, P> executeable, Function<P, Object> sequenceFuntion) {
		super();
		this.executeable = executeable;
		this.sequenceFuntion = sequenceFuntion;
	}

	/**
	 * 询问并停靠到当前服务docker上
	 * 当数据查询到后,会通知所有停靠在docker上的consumer 
	 */
	public CompletableFuture<R> inquire(P param) {
		CompletableFuture<R> future = new CompletableFuture<>();
		Object sequence = sequenceFuntion.apply(param);
		List<CompletableFuture<R>> waitingList = queryAndCreate(sequence);
		waitingList.add(future);
		if (waitingList.size() > 1) {
			return future;
		}
		try {
			CompletableFuture<R> result = executeable.execute(param);
			result.whenComplete((r, e) -> notifyAndRemove(sequence, r, e));
		} catch (Throwable ex) {
			notifyAndRemove(sequence, null, ex);
		}
		return future;
	}

	protected List<CompletableFuture<R>> queryAndCreate(Object sequence) {
		return MapUtils.getOrCreate(dockerList, sequence, linkedListSupply);
	}

	protected void notifyAndRemove(Object sequence, R result, Throwable ex) {
		List<CompletableFuture<R>> consumerList = dockerList.remove(sequence);
		for (CompletableFuture<R> future : consumerList) {
			try {
				CompletableFutureUtil.completeNormalOrThrowable(future, result, ex);
			} catch (Throwable e) {
				logger.warn("notify docker result sequence {} cause some error ", sequence, e);
			}
		}
	}
}