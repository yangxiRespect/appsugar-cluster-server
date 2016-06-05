package org.appsugar.cluster.service.binding;

import java.util.List;
import java.util.Map;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.Status;
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
	private Map<Integer, ClassMethodHelper> targetMap;
	private Map<String, List<ServiceStatusHelper>> serviceReadyInvokerMap;
	private Map<String, List<MethodInvoker>> eventInvokerMap;
	private List<RepeatInvoker> repeatInvokers;
	private Map<Class<?>, ?> serves;
	private boolean needInit = false;

	public RPCService(ServiceClusterSystem system, DistributionRPCSystemImpl rpcSystem, Map<Class<?>, ?> serves) {
		super();
		this.system = system;
		this.rpcSystem = rpcSystem;
		this.serves = serves;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		initIfNecessary(context);
		//处理服务状态改变消息
		//处pub事件消息
		//处理重复消息
		//处理方法调用消息
		return null;
	}

	protected void initIfNecessary(ServiceContext context) throws Exception {
		if (!needInit) {
			return;
		}
		needInit = true;
		//初始化接口调用方法
		targetMap = RPCSystemUtil.getClassMethodHelper(serves);

		//初始化服务准备方法
		serviceReadyInvokerMap = RPCSystemUtil.getServiceStatusHelper(serves);
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
			logger.info("try to execute  {}'s {} target {}", o.getTarget().getClass(), o.getMethod().getName(),
					o.getTarget());
			try {
				o.invoke(null);
			} catch (Throwable e) {
				logger.warn("execute default method error ", e);
			}
		});
		//处理已准备服务
		rpcSystem.serviceStatus.entrySet().stream().filter(e -> e.getValue() > 0).forEach(e -> {
			try {
				handle(new ServiceStatusMessage(e.getKey(), Status.ACTIVE), context);
			} catch (Exception ex) {
				//do nothing
			}
		});
	}

}