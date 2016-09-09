package org.appsugar.cluster.service.binding;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.appsugar.cluster.service.domain.ServiceStatusMessage;
import org.appsugar.cluster.service.domain.Status;
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

	private Set<String> createdServices = new HashSet<>();

	private ServiceDocker<Object, ServiceCreateParam> docker = new ServiceDocker<>(this::createService,
			p -> p.sequence);

	private String name;

	private int banlance;

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
		} else if (msg instanceof ServiceStatusMessage) {
			handleServiceStatusMessage((ServiceStatusMessage) msg);
		}
		return null;
	}

	/**
	 * 处理服务失效 
	 */
	protected Object handleServiceStatusMessage(ServiceStatusMessage msg) {
		if (Status.INACTIVE.equals(msg.getStatus())) {
			createdServices.remove(msg.getName());
			return true;
		}
		return false;
	}

	/**
	 * 处理服务创建消息
	 */
	protected CompletableFuture<Object> handleDynamicServiceCreateMessage(DynamicServiceCreateMessage msg)
			throws Exception {
		String sequence = msg.getSequence();
		CompletableFuture<Map<Class<?>, ?>> future = factory.create(msg.getSequence());
		CompletableFuture<Object> result = new CompletableFuture<>();
		future.whenComplete((r, e) -> {
			if (e != null) {
				result.completeExceptionally(e);
			} else {
				rpcSystem.serviceFor(r, RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence));
				result.complete(true);
			}
		});
		return result;
	}

	/**
	 * 处理创建请求
	 */
	protected Object handleDynamicServiceRequest(DynamicServiceRequest msg, ServiceContext ctx) throws Exception {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		ServiceRef leader = clusterRef.leader();
		//如果我不是leader,那么交给leader去管理
		if (leader != ctx.self()) {
			CompletableFuture<Object> future = new CompletableFuture<>();
			leader.ask(msg, e -> future.complete(e), e -> future.completeExceptionally(e));
			return future;
		}
		String sequence = msg.getSequence();
		//如果该服务已经创建成功直接返回
		if (createdServices.contains(sequence)) {
			return "Service Already Exist";
		}
		ServiceRef creator = clusterRef.balance(banlance++);
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
					createdServices.add(sequence);
				}
				CompletableFutureUtil.completeNormalOrThrowable(future, r, e);
			});
		} else {
			destination.ask(createMsg, e -> {
				createdServices.add(sequence);
				future.complete(e);
			}, e -> {
				future.completeExceptionally(e);
			});
		}
		return future;
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
