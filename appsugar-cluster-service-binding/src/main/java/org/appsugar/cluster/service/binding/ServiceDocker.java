package org.appsugar.cluster.service.binding;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.appsugar.cluster.service.domain.KeyValue;
import org.appsugar.cluster.service.util.CompletableFutureUtil;

/**
 * 服务docker
 * 解决同一请求引起多次数据请求问题
 * @author NewYoung
 * 2016年6月23日下午2:22:40
 * @param <R>
 * @param <P>
 */
public class ServiceDocker<R, P> {

	private DockerExecutable<R, P> executeable;
	private Function<P, Object> sequenceFuntion;
	private List<KeyValue<Object, List<CompletableFuture<R>>>> dockerList = new LinkedList<>();

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
		for (KeyValue<Object, List<CompletableFuture<R>>> entry : dockerList) {
			if (sequence.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		List<CompletableFuture<R>> result = new LinkedList<>();
		dockerList.add(new KeyValue<>(sequence, result));
		return result;
	}

	protected void notifyAndRemove(Object sequence, R result, Throwable ex) {
		for (Iterator<KeyValue<Object, List<CompletableFuture<R>>>> it = dockerList.iterator(); it.hasNext();) {
			KeyValue<Object, List<CompletableFuture<R>>> entry = it.next();
			if (entry.getKey().equals(sequence)) {
				it.remove();
				List<CompletableFuture<R>> consumerList = entry.getValue();
				consumerList.stream().forEach(f -> CompletableFutureUtil.completeNormalOrThrowable(f, result, ex));
				consumerList.clear();
				break;
			}
		}
	}
}