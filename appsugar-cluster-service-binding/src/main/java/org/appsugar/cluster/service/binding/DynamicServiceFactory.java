package org.appsugar.cluster.service.binding;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 动态服务工厂
 * @author NewYoung
 * 2016年6月14日下午1:46:32
 */
public interface DynamicServiceFactory {

	/**
	 * 根据参数创建服务对象
	 */
	public CompletableFuture<Map<Class<?>, ?>> create(String sequence);

	/**
	 * 服务名称
	 */
	public String service();
}
