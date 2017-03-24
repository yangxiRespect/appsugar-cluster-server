package org.appsugar.cluster.service.api;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.domain.ServiceDescriptor;

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
	 * 获取动态服务操作对象
	 * 如果服务不存在,会请求本地服务创建对应动态服务
	 */
	<T> T serviceOfDynamicLocally(Class<T> ic, String sequence);

	/**
	 * 根据接口类与对应实现类和服务名称,创建对应服务
	 */
	@Deprecated
	void serviceFor(Map<Class<?>, ?> serves, String name);

	/**
	 * 根据接口类与对应实现类和服务名称,创建服务
	 */
	@Deprecated
	void serviceFor(Map<Class<?>, ?> serves, String name, boolean local);

	/**
	 * 创建指定服务
	 * @author NewYoung
	 * 2016年12月8日下午3:04:15
	 */
	void serviceFor(ServiceDescriptor descriptor, String name);

	/**
	 * 创建指定服务
	 * @author NewYoung
	 * 2016年12月8日下午3:04:15
	 */
	CompletableFuture<Void> serviceForAsync(ServiceDescriptor descriptor, String name);

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
	 *只能关闭本地服务
	 *{@link this#stop(Object)}
	 */
	@Deprecated
	void stop(String name);

	/**
	 * 停止一个服务
	 * 只能关闭本地服务,必须传入一个服务对象
	 * @author NewYoung
	 * 2017年3月24日下午2:00:06
	 */
	<T> void stop(T service);

	/**
	 * 关闭服务
	 */
	void terminate();

	/**
	 * 注册服务提供工厂
	 */
	void registerFactory(DynamicServiceFactory factory);

	/**
	 * 返回服务所有服务引用
	 * 结果不允许修改
	 * @author NewYoung
	 * 2016年12月5日下午4:03:07
	 */
	Collection<ServiceRef> serviceRefs();

}