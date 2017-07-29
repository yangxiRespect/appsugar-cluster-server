package org.appsugar.cluster.service.binding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.CommandMessage;
import org.appsugar.cluster.service.domain.MethodInvokeMessage;
import org.appsugar.cluster.service.domain.MethodInvokeOptimizingMessage;
import org.appsugar.cluster.service.domain.MethodInvokeOptimizingResponse;
import org.appsugar.cluster.service.domain.RepeatMessage;
import org.appsugar.cluster.service.domain.ServiceStatusMessage;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.domain.SubscribeMessage;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程方法调用服务
 * @author NewYoung
 * 2016年6月4日上午1:18:11
 */
public class RPCService implements Service {
	private static final Logger logger = LoggerFactory.getLogger(RPCService.class);
	private ServiceClusterSystem system;
	private DistributionRPCSystemImpl rpcSystem;
	private Map<MethodInvoker, Integer> sequenceMap = new HashMap<>();
	private Map<Integer, MethodInvoker> optimizingMethodInvokerMap = new HashMap<>();
	private Map<List<String>, MethodInvoker> methodInvokerMap;
	private Map<String, List<ServiceStatusHelper>> serviceReadyInvokerMap;
	private Map<String, List<MethodInvoker>> eventInvokerMap;
	private List<RepeatInvoker> repeatInvokers;
	private List<MethodInvoker> closeInvoker;
	private Map<Class<?>, ?> serves;
	private int sequence = 0;
	private boolean needInit = false;
	private boolean stopped = false;
	ServiceRef self;

	public RPCService(ServiceClusterSystem system, DistributionRPCSystemImpl rpcSystem, Map<Class<?>, ?> serves) {
		super();
		this.system = system;
		this.rpcSystem = rpcSystem;
		this.serves = serves;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Throwable {
		if (stopped) {
			logger.error("service {} was closed can't handle message anymore {}", context.self(), msg);
			return false;
		}
		initIfNecessary(context);
		if (msg instanceof MethodInvokeOptimizingMessage) {
			return processMethodInvokeOptimizingMessage((MethodInvokeOptimizingMessage) msg);
		}
		//处理服务状态改变消息
		if (msg instanceof ServiceStatusMessage) {
			processServiceStatusMessage((ServiceStatusMessage) msg);
		}
		//处pub事件消息
		else if (msg instanceof SubscribeMessage) {
			processSubscribeMessage((SubscribeMessage) msg);
		}
		//处理重复消息
		else if (msg instanceof RepeatMessage) {
			processRepeatMessage();
		}
		//处理方法调用消息
		else if (msg instanceof MethodInvokeMessage) {
			return processMethodInvokeMessage((MethodInvokeMessage) msg);
		}
		//处理命令消息
		else if (msg instanceof CommandMessage) {
			return processCommandMessage((CommandMessage) msg, context);
		}
		//处理批处理消息
		else if (msg instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) msg;
			for (Object m : list) {
				handle(m, context);
			}
		}
		return null;
	}

	/**
	 * 处理服务变更消息
	 */
	protected void processServiceStatusMessage(ServiceStatusMessage msg) throws Throwable {
		String name = msg.getServiceRef().name();
		List<ServiceStatusHelper> helperList = serviceReadyInvokerMap.get(name);
		if (helperList == null) {
			return;
		}
		ServiceClusterRef clusterRef = system.serviceOf(name);
		if (clusterRef != null && clusterRef.size() > 1) {
			//服务数大于1，不做任何处理
			return;
		}
		Status status = msg.getStatus();
		for (ServiceStatusHelper helper : helperList) {
			try {
				helper.tryInvoke(rpcSystem, status);
			} catch (Exception ex) {
				logger.warn("execute  {} error  ", helper.getMethod(), ex);
			}
		}
	}

	/**
	 * 处理事件关注消息
	 */
	protected void processSubscribeMessage(SubscribeMessage msg) throws Throwable {
		String topic = msg.getTopic();
		List<MethodInvoker> invokerList = eventInvokerMap.get(topic);
		if (invokerList == null) {
			return;
		}
		Object param = msg.getData();
		if (param == null) {
			//null event? kidding me
			return;
		}
		for (MethodInvoker invoker : invokerList) {
			Class<?>[] types = invoker.getParameterTypes();
			if (!types[0].isAssignableFrom(param.getClass())) {
				continue;
			}
			try {
				invoker.invoke(new Object[] { param });
			} catch (Exception ex) {
				logger.warn("execute  {} is {} ", invoker.getMethod(), ex);
			}
		}
	}

	/**
	 * 处理重复消息
	 */
	protected void processRepeatMessage() throws Throwable {
		for (RepeatInvoker invoker : repeatInvokers) {
			try {
				invoker.tryInvoke(System.currentTimeMillis());
			} catch (Exception ex) {
				logger.warn("execute {} error {} ", invoker.getInvoker().getMethod(), ex);
			}
		}
	}

	/**
	 * 处理方法调用消息
	 */
	protected Object processMethodInvokeMessage(MethodInvokeMessage msg) throws Throwable {
		MethodInvoker invoker = methodInvokerMap.get(msg.getNameList());
		if (invoker == null) {
			throw new RuntimeException("method not found " + msg.getNameList());
		}
		Object result = invoker.invoke(msg.getParams());
		Integer invokerSequence = sequenceMap.get(invoker);
		if (invokerSequence == null) {
			invokerSequence = sequence++;
			sequenceMap.put(invoker, invokerSequence);
			optimizingMethodInvokerMap.put(invokerSequence, invoker);
		}
		Integer finalSequence = invokerSequence;
		if (result instanceof CompletionStage) {
			CompletableFuture<MethodInvokeOptimizingResponse> future = new CompletableFuture<>();
			@SuppressWarnings("unchecked")
			CompletionStage<Object> f = (CompletionStage<Object>) result;
			f.whenComplete((r, e) -> {
				if (e != null) {
					//如果出现异常,不进行方法调用优化.
					future.completeExceptionally(e);
				} else {
					future.complete(new MethodInvokeOptimizingResponse(finalSequence, r));
				}
			});
			return future;
		}
		//告诉客户端调用优化
		return new MethodInvokeOptimizingResponse(invokerSequence, result);
	}

	/**
	 * 处理方法调用消息
	 */

	protected Object processMethodInvokeOptimizingMessage(MethodInvokeOptimizingMessage msg) throws Throwable {
		MethodInvoker invoker = optimizingMethodInvokerMap.get(msg.getSequence());
		if (invoker == null) {
			throw new RuntimeException("method not found " + msg.getSequence());
		}
		return invoker.invoke(msg.getParams());
	}

	/**
	 * 处理命令消息
	 * @author NewYoung
	 * 2017年5月10日上午10:13:47
	 */
	protected Object processCommandMessage(CommandMessage commandMessage, ServiceContext context) {
		if (Objects.equals(CommandMessage.CLOSE_COMMAND, commandMessage.getCmd())) {
			logger.info("prepar to stop self manully self {}    sender {}", context.self(), context.sender());
			context.system().stop(context.self());
			stopped = true;
			closeInvoker.stream().forEach(e -> {
				try {
					e.invoke(null);
				} catch (Throwable ex) {
					logger.error("invoke service close method error ", ex);
				}
			});
		} else {
			return false;
		}
		return true;
	}

	protected void initIfNecessary(ServiceContext context) throws Exception {
		if (needInit) {
			return;
		}
		needInit = true;

		methodInvokerMap = RPCSystemUtil.getClassMethodInvoker(serves);
		//初始化服务准备方法
		serviceReadyInvokerMap = RPCSystemUtil.getServiceStatusHelper(serves);
		//设置我关注的服务
		serviceReadyInvokerMap.keySet().stream().forEach(this.system::focusNormalService);
		//初始化事件调用方法
		eventInvokerMap = RPCSystemUtil.getEventMethodInvoke(serves);
		eventInvokerMap.keySet().stream().forEach(e -> system.subscribe(e, context.self()));
		//初始化重复调用方法
		repeatInvokers = RPCSystemUtil.getRepeatInvoker(serves);
		if (!repeatInvokers.isEmpty()) {
			system.schedule(context.self(), repeatInvokers.stream().mapToLong(m -> m.getInterval()).min().getAsLong(),
					RepeatMessage.instance);
		}
		//初始化默认调用方法
		RPCSystemUtil.getDefaultInvoker(serves).stream().forEach(o -> {
			try {
				o.invoke(null);
			} catch (Throwable e) {
				logger.warn("execute default method error ", e);
			}
		});
		//关注服务动态
		serviceReadyInvokerMap.entrySet().forEach(e -> rpcSystem.getServiceRefAndCreateFocusOn(e.getKey()).add(self));
		//处理已准备服务
		for (String serviceName : serviceReadyInvokerMap.keySet()) {
			ServiceClusterRef refs = rpcSystem.system.serviceOf(serviceName);
			if (refs == null || refs.size() == 0) {
				continue;
			}
			try {
				handle(new ServiceStatusMessage(refs.one(), Status.ACTIVE), context);
			} catch (@SuppressWarnings("unused") Throwable ex) {
			}
		}
		//处理关闭方法
		closeInvoker = RPCSystemUtil.getCloseInvoker(serves);
	}

}