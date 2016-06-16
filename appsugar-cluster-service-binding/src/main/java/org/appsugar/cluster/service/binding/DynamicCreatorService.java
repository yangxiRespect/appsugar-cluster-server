package org.appsugar.cluster.service.binding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.api.Status;

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

	private Map<String, List<CompletableFuture<Object>>> serviceWaiting = new HashMap<>();

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
			return handleDynamicServiceCreateMessage((DynamicServiceCreateMessage) msg, context);
		} else if (msg instanceof ServiceStatusMessage) {
			handleServiceStatusMessage((ServiceStatusMessage) msg, context);
		}
		return null;
	}

	/**
	 * 处理服务失效 
	 */
	protected Object handleServiceStatusMessage(ServiceStatusMessage msg, ServiceContext ctx) {
		if (Status.INACTIVE.equals(msg.getStatus())) {
			createdServices.remove(msg.getName());
			return true;
		}
		return false;
	}

	/**
	 * 处理服务创建消息
	 */
	protected CompletableFuture<Object> handleDynamicServiceCreateMessage(DynamicServiceCreateMessage msg,
			ServiceContext ctx) throws Exception {
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
		List<CompletableFuture<Object>> waiting = serviceWaiting.get(sequence);
		//如果服务正在创建中,那么加入创建列表中
		if (waiting != null) {
			CompletableFuture<Object> future = new CompletableFuture<>();
			waiting.add(future);
			return future;
		}
		ServiceRef creator = clusterRef.balance(banlance++);
		DynamicServiceCreateMessage createMsg = new DynamicServiceCreateMessage(msg.getSequence());
		CompletableFuture<Object> result = new CompletableFuture<>();
		serviceWaiting.put(sequence, new LinkedList<>());
		//如果论到自己创建服务,那么直接处理创建消息
		if (creator == ctx.self()) {
			try {
				CompletableFuture<Object> future = (CompletableFuture<Object>) handle(createMsg, ctx);
				future.whenComplete((r, e) -> {
					if (e != null) {
						result.completeExceptionally(e);
						notifyWaiting(sequence, null, e);
					} else {
						createdServices.add(sequence);
						result.complete(r);
						notifyWaiting(sequence, r, null);
					}
				});
			} catch (Exception ex) {
				throw ex;
			}
		} else {
			//请求创建服务并通知等候列表
			creator.ask(createMsg, e -> {
				createdServices.add(sequence);
				result.complete(e);
				notifyWaiting(sequence, e, null);
			}, e -> {
				result.completeExceptionally(e);
				notifyWaiting(sequence, null, e);
			});
		}
		return result;
	}

	private void notifyWaiting(String sequence, Object result, Throwable ex) {
		List<CompletableFuture<Object>> futureList = serviceWaiting.remove(sequence);
		if (futureList == null) {
			return;
		}
		for (CompletableFuture<Object> future : futureList) {
			if (ex != null) {
				future.completeExceptionally(ex);
			} else {
				future.complete(result);
			}
		}
	}

}
