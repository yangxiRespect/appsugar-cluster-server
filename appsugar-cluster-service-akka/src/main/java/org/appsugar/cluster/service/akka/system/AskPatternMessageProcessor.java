package org.appsugar.cluster.service.akka.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.appsugar.cluster.service.akka.domain.AskPatternEvent;
import org.appsugar.cluster.service.akka.domain.AskPatternException;
import org.appsugar.cluster.service.akka.domain.AskPatternRequest;
import org.appsugar.cluster.service.akka.domain.AskPatternResponse;
import org.appsugar.cluster.service.akka.domain.RepeatEvent;
import org.appsugar.cluster.service.akka.domain.RequestMarker;
import org.appsugar.cluster.service.domain.ServiceException;
import org.appsugar.cluster.service.util.MapUtils;
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
	private static final RepeatEvent repeatEvent = RepeatEvent.INSTANCE;
	private static final int repeatEventIdleMaxTimes = 10;
	private static final Supplier<List<RequestMarker<?>>> supplier = LinkedList::new;
	private Map<ActorRef, List<RequestMarker<?>>> refMarkerMap = new HashMap<>();
	private Integer requestSequence = 0;
	private Integer waitingCount = 0;

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
			processRepeatEvent();
		} else if (msg instanceof AskPatternException) {
			logger.error("remote service cause internal exception  {}", ((AskPatternException) msg).getMsg());
		} else {
			return processOtherMessage(msg, ctx);
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
		ActorRef destination = event.getDestination();
		AskPatternRequest request = new AskPatternRequest(sequence, event.getMsg());
		destination.tell(request, ctx.getSelf());

		RequestMarker<?> marker = new RequestMarker<>(sequence, startTime, endTime, event.getFuture());
		getRequestMarkerList(destination).add(marker);
		increaseWaiting();
		//发起请求后,检测是否需要触发定时任务
		scheduleIfNecessary(ctx);
	}

	/**
	 * 处理来自其他服务的请求
	 */
	@SuppressWarnings("unchecked")
	protected void processRequest(AskPatternRequest req, ProcessorContext ctx) {
		int sequence = req.getSequence();
		Object msg = req.getData();
		ActorRef sender = ctx.getSender();
		try {
			Object result = ctx.processNext(msg);
			if (result instanceof CompletionStage) {
				//如果返回的是future,那么对future做特殊处理
				CompletionStage<Object> future = (CompletionStage<Object>) result;
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
		RequestMarker<?> marker = removeRequestMarkerBySequence(ctx.getSender(), sequence);
		if (marker == null) {
			return;
		}
		decreaseWaiting();
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
	protected void processRepeatEvent() {
		if (waitingCount < 1) {
			//fix memory leak 
			refMarkerMap.clear();
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
		for (List<RequestMarker<?>> markerList : refMarkerMap.values()) {
			for (Iterator<RequestMarker<?>> it = markerList.iterator(); it.hasNext();) {
				RequestMarker<?> marker = it.next();
				if (marker.getEndTime() > current) {
					continue;
				}
				it.remove();
				decreaseWaiting();
				marker.getFuture().completeExceptionally(new TimeoutException(
						"request time out sequence " + marker.getSequence() + " start at" + marker.getStartTime()));
			}
		}
	}

	/**
	 * 处理其他消息
	 */
	protected Object processOtherMessage(Object msg, ProcessorContext ctx) throws Throwable {
		if (msg instanceof Exception) {
			AskPatternException exception = new AskPatternException(getExceptionMessage((Exception) msg));
			//异常信息直接发送回原来地方
			ctx.getSender().tell(exception, ctx.getSelf());
			return null;
		}
		//直接交给下一个处理器处理
		return ctx.processNext(msg);
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
	 * 获取顶级异常信息
	 */
	private String getExceptionMessage(Throwable e) {
		Throwable cause;
		Throwable root = e;
		while ((cause = e.getCause()) != null) {
			root = cause;
		}
		return root.getMessage();
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
				Duration.create(5000, TimeUnit.MILLISECONDS), self, repeatEvent, ctx.getSystem().dispatcher(),
				ActorRef.noSender());
	}

	private RequestMarker<?> removeRequestMarkerBySequence(ActorRef ref, int sequence) {
		List<RequestMarker<?>> markerList = getRequestMarkerList(ref);
		for (Iterator<RequestMarker<?>> it = markerList.iterator(); it.hasNext();) {
			RequestMarker<?> marker = it.next();
			if (marker.getSequence() == sequence) {
				it.remove();
				return marker;
			}
		}
		return null;
	}

	private List<RequestMarker<?>> getRequestMarkerList(ActorRef ref) {
		return MapUtils.getOrCreate(refMarkerMap, ref, supplier);
	}

	private int increaseWaiting() {
		return ++waitingCount;
	}

	private int decreaseWaiting() {
		return --waitingCount;
	}
}