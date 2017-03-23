package org.appsugar.cluster.service.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.DynamicServiceCreateMessage;
import org.appsugar.cluster.service.domain.DynamicServiceRequest;
import org.appsugar.cluster.service.domain.ServiceDescriptor;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.RPCSystemUtil;

/**
 * 动态创建服务
 * @author NewYoung
 * 2016年6月14日下午2:45:45
 */
public class DynamicCreatorService implements Service {

	private DynamicServiceFactory factory;

	private ServiceClusterSystem system;

	private DistributionRPCSystem rpcSystem;

	private ServiceDocker<Object, ServiceCreateParam> docker = new ServiceDocker<>(this::createService,
			p -> p.sequence);

	private Map<ServiceRef, Integer> serviceBalance = new HashMap<>(48);

	/**服务名称**/
	private String name;

	public DynamicCreatorService(DynamicServiceFactory factory, ServiceClusterSystem system,
			DistributionRPCSystem rpcSystem, String name) {
		super();
		this.factory = factory;
		this.system = system;
		this.rpcSystem = rpcSystem;
		this.name = name;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		if (msg instanceof DynamicServiceRequest) {
			return handleDynamicServiceRequest((DynamicServiceRequest) msg, context);
		} else if (msg instanceof DynamicServiceCreateMessage) {
			return handleDynamicServiceCreateMessage((DynamicServiceCreateMessage) msg);
		}
		return null;
	}

	/**
	 * 处理服务创建消息
	 */
	protected CompletableFuture<Void> handleDynamicServiceCreateMessage(DynamicServiceCreateMessage msg)
			throws Exception {
		String sequence = msg.getSequence();
		CompletableFuture<Void> result = new CompletableFuture<>();
		//异步创建出服务者
		CompletableFuture<ServiceDescriptor> future = RPCSystemUtil
				.wrapContextFuture(factory.create(msg.getSequence()));
		future.whenComplete((r, e) -> {
			if (e != null) {
				result.completeExceptionally(e);
			} else {
				//根据服务者,异步创建服务
				r.setLocal(factory.local());
				rpcSystem.serviceForAsync(r, RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence))
						.whenComplete((r1, e1) -> {
							CompletableFutureUtil.completeNormalOrThrowable(result, r1, e1);
						});
			}
		});
		return result;

	}

	/**
	 * 处理创建请求
	 */
	protected Object handleDynamicServiceRequest(DynamicServiceRequest msg, ServiceContext ctx) throws Exception {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		String sequence = msg.getSequence();
		//如果该服务已经创建成功直接返回
		String expectedServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence);
		ServiceClusterRef expectedServiceClusterRef = system.serviceOf(expectedServiceName);
		if (Objects.nonNull(expectedServiceClusterRef) && Objects.nonNull(expectedServiceClusterRef.one())) {
			return "Service already exist";
		}
		ServiceRef creator = null;
		if (msg.isLocation()) {
			creator = RPCSystemUtil.getLocalServiceRef(clusterRef);
		} else {
			ServiceRef leader = clusterRef.leader();
			//如果我不是leader,那么交给leader去管理
			if (leader != ctx.self()) {
				CompletableFuture<Object> future = new CompletableFuture<>();
				leader.ask(msg, e -> future.complete(e), e -> future.completeExceptionally(e));
				return future;
			}
			creator = pickupServiceRef(clusterRef);
		}
		return docker.inquire(new ServiceCreateParam(creator, ctx, sequence));
	}

	/**
	 * 创建服务逻辑
	 */
	protected CompletableFuture<Object> createService(ServiceCreateParam param) throws Throwable {
		CompletableFuture<Object> future = new CompletableFuture<>();
		ServiceRef destination = param.destination;
		ServiceContext context = param.context;
		ServiceRef self = context.self();
		String sequence = param.sequence;
		DynamicServiceCreateMessage createMsg = new DynamicServiceCreateMessage(sequence);
		if (destination == self) {
			@SuppressWarnings("unchecked")
			CompletableFuture<Object> f = (CompletableFuture<Object>) handle(createMsg, context);
			f.whenComplete((r, e) -> {
				if (e == null) {
				}
				CompletableFutureUtil.completeNormalOrThrowable(future, r, e);
			});
		} else {
			destination.ask(createMsg, e -> {
				future.complete(e);
			}, e -> {
				future.completeExceptionally(e);
			});
		}
		return future;
	}

	/**
	 * 筛选出服务数最小的服务
	 * @author NewYoung
	 * 2017年3月23日下午4:07:38
	 */
	private ServiceRef pickupServiceRef(ServiceClusterRef cluster) {
		ServiceRef min = cluster.one();
		int minValue = serviceBalance.getOrDefault(min, 0);
		for (ServiceRef ref : cluster.iterable()) {
			Integer value = serviceBalance.getOrDefault(ref, 0);
			if (value < minValue) {
				min = ref;
				minValue = value;
			}
		}
		serviceBalance.put(min, minValue + 1);
		return min;
	}

	private static class ServiceCreateParam {
		public ServiceRef destination;
		public ServiceContext context;
		public String sequence;

		public ServiceCreateParam(ServiceRef destination, ServiceContext context, String sequence) {
			super();
			this.destination = destination;
			this.context = context;
			this.sequence = sequence;
		}
	}
}
