package org.appsugar.cluster.service.akka.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.appsugar.cluster.service.api.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Scheduler;
import scala.concurrent.duration.Duration;

/**
 * ask模式消息处理
 * 处理AskPatternEvent,AskPatternRequest,AskPatternResponse,RepeatEvent
 * @author NewYoung
 * 2016年5月29日下午11:09:44
 */
public class AskPatternMessageProcessor implements MessageProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AskPatternMessageProcessor.class);
	private static final RepeatEvent repeatEvent = new RepeatEvent();
	private static final int repeatEventIdleMaxTimes = 10;
	private Map<Integer, RequestMarker<?>> markers = new HashMap<>();

	//可以做性能极致优化,针对每一个目的地存储一个序列,序列控制在0~65535 减少网络传输
	private Integer requestSequence = 0;

	//重复消息取消接口
	private Cancellable cancellable;

	//重复消息闲置次数
	private int repeatEventIdleTimes = 0;

	@Override
	public Object process(ProcessorContext ctx, Object msg) throws Throwable {
		if (msg instanceof AskPatternEvent) {
			processAskEvent((AskPatternEvent<?>) msg, ctx);
		} else if (msg instanceof AskPatternRequest) {
			processRequest((AskPatternRequest) msg, ctx);
		} else if (msg instanceof AskPatternResponse) {
			processResponse((AskPatternResponse) msg, ctx);
		} else if (msg instanceof RepeatEvent) {
			processRepeatEvent((RepeatEvent) msg, ctx);
		} else {
			processOtherMessage(msg, ctx);
		}
		return null;
	}

	/**
	 * 处理发送请求,像目的地发起一个ask pattern的请求
	 * @param event 请求事件消息
	 * @param ctx 当前actor上下文
	 */
	protected void processAskEvent(AskPatternEvent<?> event, ProcessorContext ctx) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + event.getTimeout();
		int sequence = requestSequence++;
		RequestMarker<?> marker = new RequestMarker<>(startTime, endTime, event.getFuture());
		markers.put(sequence, marker);
		ActorRef destination = event.getDestination();
		AskPatternRequest request = new AskPatternRequest(sequence, event.getMsg());
		destination.tell(request, ctx.getSelf());
		scheduleIfNecessary(ctx);
	}

	/**
	 * 处理来自其他服务的请求
	 */
	protected void processRequest(AskPatternRequest req, ProcessorContext ctx) {
		int sequence = req.getSequence();
		Object msg = req.getData();
		ActorRef sender = ctx.getSender();
		try {
			Object result = ctx.processNext(msg);
			if (result instanceof CompletableFuture) {
				//如果返回的是future,那么对future做特殊处理
				@SuppressWarnings("unchecked")
				CompletableFuture<Object> future = (CompletableFuture<Object>) result;
				future.whenComplete((r, e) -> {
					Object responseData = e == null ? r : e;
					response(sequence, responseData, sender, ctx.getSelf());
				});
				return;
			}
			response(sequence, result, sender, ctx.getSelf());
		} catch (Throwable e) {
			//执行过程中出现了异常把异常返回
			response(sequence, e, sender, ctx.getSelf());
		}
	}

	/**
	 * 处理来自其他服务的响应
	 */
	protected void processResponse(AskPatternResponse res, ProcessorContext ctx) {
		int sequence = res.getSequence();
		Object data = res.getData();
		RequestMarker<?> marker = markers.remove(sequence);
		if (marker == null) {
			//TODO 设置日志是否打印
			logger.warn("Can't process response  because request has timeout sequence {} destination {}", sequence,
					ctx.getSender());
			return;
		}
		@SuppressWarnings("unchecked")
		CompletableFuture<Object> future = (CompletableFuture<Object>) marker.getFuture();
		if (data instanceof AskPatternException) {
			future.completeExceptionally(new ServiceException(((AskPatternException) data).getMsg()));
			return;
		}
		future.complete(data);
	}

	/**
	 * 处理系统重复消息
	 */
	protected void processRepeatEvent(RepeatEvent event, ProcessorContext ctx) {
		if (markers.isEmpty()) {
			if (cancellable == null) {
				return;
			}
			repeatEventIdleTimes++;
			if (repeatEventIdleTimes < repeatEventIdleMaxTimes) {
				return;
			}
			repeatEventIdleTimes = 0;
			//取消自身重复消息
			cancellable.cancel();
			//直接清空
			cancellable = null;
			return;
		}
		long current = System.currentTimeMillis();
		for (Iterator<Entry<Integer, RequestMarker<?>>> it = markers.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, RequestMarker<?>> entry = it.next();
			RequestMarker<?> marker = entry.getValue();
			if (marker.getEndTime() < current) {
				continue;
			}
			it.remove();
			marker.getFuture().completeExceptionally(new TimeoutException(
					"request time out sequence " + entry.getKey() + " start at" + marker.getStartTime()));
		}
	}

	/**
	 * 处理其他消息
	 */
	protected void processOtherMessage(Object msg, ProcessorContext ctx) throws Throwable {
		//直接交给下一个处理器处理
		ctx.processNext(msg);
	}

	/**
	 * 发送响应消息
	 */
	private void response(int sequence, Object data, ActorRef destination, ActorRef self) {
		Object responseData = data;
		if (data instanceof Throwable) {
			logger.warn("process request failed sequence {}  destination {}  ex {}", sequence, destination, data);
			responseData = new AskPatternException(getExceptionMessage((Throwable) data));
		}
		destination.tell(new AskPatternResponse(sequence, responseData), self);
	}

	/**
	 * 组装异常消息
	 */
	private String getExceptionMessage(Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage());
		Throwable cause = e.getCause();
		while (cause != null) {
			sb.append(" : ");
			sb.append(cause.getMessage());
			cause = cause.getCause();
		}
		return sb.toString();
	}

	/**
	 * 是否需要创建定时repeat消息
	 */
	private void scheduleIfNecessary(ProcessorContext ctx) {
		if (cancellable != null) {
			return;
		}
		Scheduler scheduler = ctx.getSystem().scheduler();
		ActorRef self = ctx.getSelf();
		cancellable = scheduler.schedule(Duration.create(100, TimeUnit.MILLISECONDS),
				Duration.create(100, TimeUnit.MILLISECONDS), self, repeatEvent, ctx.getSystem().dispatcher(), null);
	}
}