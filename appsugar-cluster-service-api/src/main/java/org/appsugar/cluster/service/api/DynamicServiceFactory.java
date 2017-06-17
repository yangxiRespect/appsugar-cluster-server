package org.appsugar.cluster.service.api;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.domain.ServiceDescriptor;

/**
 * 动态服务工厂
 * @author NewYoung
 * 2016年6月14日下午1:46:32
 */
public interface DynamicServiceFactory {

	/**
	 * 根据参数创建服务对象
	 */
	public CompletableFuture<ServiceDescriptor> create(String sequence);

	/**
	 * 服务名称
	 * @author NewYoung
	 * 2016年12月8日下午3:01:14
	 */
	public String name();

	/**
	 * 是否本地动态服务工厂
	 * @author NewYoung
	 * 2016年12月11日下午2:57:45
	 */
	public default boolean local() {
		return false;
	}

	/**
	 * 动态工厂初始化
	 * @author NewYoung
	 * 2017年4月27日下午1:26:02
	 */
	@SuppressWarnings("unused")
	public default void init(DistributionRPCSystem system) {
		//do nothing
	}
}
