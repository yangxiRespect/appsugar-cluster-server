package org.appsugar.cluster.service.binding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;

/**
 * 动态创建服务
 * @author NewYoung
 * 2016年6月14日下午2:45:45
 */
public class DynamicCreatorService implements Service {

	private DynamicServiceFactory factory;

	private ServiceClusterSystem system;

	private Set<String> createdServices = new HashSet<>();

	private Map<String, List<CompletableFuture<Object>>> serviceWaiting = new HashMap<>();

	private String name;

	private int banlance;

	public DynamicCreatorService(DynamicServiceFactory factory, ServiceClusterSystem system, String name) {
		super();
		this.factory = factory;
		this.system = system;
		this.name = name;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		if (msg instanceof DynamicServiceRequest) {
			return handleDynamicServiceRequest((DynamicServiceRequest) msg, context);
		} else if (msg instanceof DynamicServiceCreateMessage) {

		} else if (msg instanceof ServiceStatusMessage) {

		}
		return null;
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
			return null;
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

		return null;
	}
}
