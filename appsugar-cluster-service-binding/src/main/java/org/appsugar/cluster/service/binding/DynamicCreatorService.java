package org.appsugar.cluster.service.binding;

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

	private String name;

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

		}
		return null;
	}

	/**
	 * 处理创建请求
	 */
	protected Object handleDynamicServiceRequest(DynamicServiceRequest msg, ServiceContext ctx) throws Exception {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		ServiceRef min = clusterRef.min();
		if (min != ctx.self()) {
			CompletableFuture<Object> future = new CompletableFuture<>();
			min.ask(msg, e -> future.complete(e), e -> future.completeExceptionally(e));
			return future;
		}
		return null;
	}
}
