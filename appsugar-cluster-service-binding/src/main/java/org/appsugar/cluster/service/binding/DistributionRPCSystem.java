package org.appsugar.cluster.service.binding;

import java.util.Map;

import org.appsugar.cluster.service.api.ServiceRef;

/**
 * 分布式方法调用系统
 * @author NewYoung
 * 2016年6月4日上午12:35:28
 */
public interface DistributionRPCSystem {

	/**
	 *根据接口类获取对应操作对象 
	 */
	<T> T serviceOf(Class<T> ic);

	/**
	 * 根据接口类获取操作对象 ,如果服务不存在,返回null
	 */
	<T> T serviceOfIfPresent(Class<T> ic);

	/**
	 * 获取动态服务操作对象
	 * 如果不存在,那么返回null
	 */
	<T> T serviceOfDynamicIfPresent(Class<T> ic, String sequence);

	/**
	 * 获取动态服务操作对象
	 * 如果服务不存在,会请求对应动态服务创建工厂创建对应服务
	 */
	<T> T serviceOfDynamic(Class<T> ic, String sequence);

	/**
	 * 根据接口类与对应实现类和服务名称,创建对应服务
	 */
	void serviceFor(Map<Class<?>, ?> serves, String name);

	/**
	 * 添加一个服务监听器
	 */
	boolean addServiceListener(ServiceListener listener);

	/**
	 *移除一个服务监听器
	 */
	boolean removeServiceListener(ServiceListener listener);

	/**
	 * 发布事件
	 */
	void publish(Object msg, String topic);

	/**
	 *停用一个服务
	 */
	void stop(ServiceRef ref);

	/**
	 * 关闭服务
	 */
	void terminate();

	/**
	 * 注册服务提供工厂
	 */
	void registerFactory(DynamicServiceFactory factory);

}