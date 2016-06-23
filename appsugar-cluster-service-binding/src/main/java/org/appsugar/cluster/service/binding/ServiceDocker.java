package org.appsugar.cluster.service.binding;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.appsugar.cluster.service.api.KeyValue;

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
	private List<KeyValue<Object, List<BiConsumer<R, Throwable>>>> dockerList = new LinkedList<>();

	public ServiceDocker(DockerExecutable<R, P> executeable, Function<P, Object> sequenceFuntion) {
		super();
		this.executeable = executeable;
		this.sequenceFuntion = sequenceFuntion;
	}

	/**
	 * 询问并停靠到当前服务docker上
	 * 当数据查询到后,会通知所有停靠在docker上的consumer 
	 */
	public void inquire(P param, BiConsumer<R, Throwable> consumer) {
		Object sequence = sequenceFuntion.apply(param);
		List<BiConsumer<R, Throwable>> waitingList = queryAndCreate(sequence);
		waitingList.add(consumer);
		if (waitingList.size() > 1) {
			return;
		}
		try {
			CompletableFuture<R> future = executeable.execute(param);
			future.whenComplete((r, e) -> notifyAndRemove(sequence, r, e));
		} catch (Throwable ex) {
			notifyAndRemove(sequence, null, ex);
		}
	}

	protected List<BiConsumer<R, Throwable>> queryAndCreate(Object sequence) {
		for (KeyValue<Object, List<BiConsumer<R, Throwable>>> entry : dockerList) {
			if (sequence.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		List<BiConsumer<R, Throwable>> result = new LinkedList<>();
		dockerList.add(new KeyValue<>(sequence, result));
		return result;
	}

	protected void notifyAndRemove(Object sequence, R result, Throwable ex) {
		for (Iterator<KeyValue<Object, List<BiConsumer<R, Throwable>>>> it = dockerList.iterator(); it.hasNext();) {
			KeyValue<Object, List<BiConsumer<R, Throwable>>> entry = it.next();
			if (entry.getKey().equals(sequence)) {
				it.remove();
				List<BiConsumer<R, Throwable>> consumerList = entry.getValue();
				consumerList.stream().forEach(e -> {
					try {
						e.accept(result, ex);
					} catch (Throwable internalException) {
						//DO NOTHING
					}
				});
				consumerList.clear();
			}
		}
	}
}