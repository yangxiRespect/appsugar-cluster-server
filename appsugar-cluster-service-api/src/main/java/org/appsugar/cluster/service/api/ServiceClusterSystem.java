package org.appsugar.cluster.service.api;

import java.util.concurrent.CompletableFuture;

/**
 * 服务集群系统
 * 可注册服务,搜索服务
 * @author NewYoung
 * 2016年5月23日下午1:58:53
 */
public interface ServiceClusterSystem extends SubPubClusterSystem {

	/**
	 * 向服务中心注册一个服务
	 * @param service 服务instance,一个instance只允许注册一次
	 * @param name 服务名称
	 * @return 返回注册后,该服务的引用
	 */
	ServiceRef serviceFor(Service service, String name);

	/**
	 * 根据名称搜索服务集群引用
	 */
	ServiceClusterRef serviceOf(String name);

	/**
	 * 获取所有服务集群引用
	 */
	Iterable<ServiceClusterRef> services();

	/**
	 * 关闭当前集群服务系统
	 */
	CompletableFuture<Void> terminate();

	/**
	 * 计划任务,每 time 毫秒调用一次 {@link ServiceRef#tell(msg, ServiceRef.NO_SENDER)}
	 */
	Cancelable schedule(ServiceRef serviceRef, long time, Object msg);

}