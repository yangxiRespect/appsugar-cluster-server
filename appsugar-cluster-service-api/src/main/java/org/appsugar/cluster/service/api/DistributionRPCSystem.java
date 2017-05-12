package org.appsugar.cluster.service.api;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.domain.ServiceDescriptor;

/**
 * 分布式方法调用系统 
 * @author NewYoung
 * 2016年6月4日上午12:35:28
 */
public interface DistributionRPCSystem {

	/**
	 * 设置系统需要服务.
	 * <pre>
	 * 		使用服务前需先声明.
	 * 		使用动态服务前需先声明
	 * 		系统把所需服务告知其他节点,并获取服务引用.
	 * </pre>
	 * @author NewYoung
	 * 2017年5月12日下午1:15:39
	 */
	void require(String name);

	/**
	 * 设置系统需要服务
	 * {@link this#require(String)}
	 * @author NewYoung
	 * 2017年5月12日下午1:34:38
	 */
	void require(Class<?> clazz);

	/**
	 * 判断该服务是否存在于集群中
	 * @author NewYoung
	 * 2017年5月5日下午4:03:29
	 */
	boolean exist(String name);

	/**
	 * 判断该服务是否存在于当前节点中
	 * @author NewYoung
	 * 2017年5月5日下午4:03:45
	 */
	boolean existLocally(String name);

	/**
	 * 根据接口类获取对应操作对象
	 * @see this{@link #require(Class)}
	 */
	<T> T serviceOf(Class<T> ic);

	/**
	 * 查找动态服务是否存在
	 * @author NewYoung
	 * 2017年5月12日下午2:24:15
	 */
	<T> CompletableFuture<T> serviceOfDynamicIfPresent(Class<T> ic, String sequence);

	/**
	 * 获取动态服务操作对象
	 * 如果服务不存在,会请求对应动态服务创建工厂创建对应服务(服务可能创建在任意节点中)
	 */
	<T> CompletableFuture<T> serviceOfDynamic(Class<T> ic, String sequence);

	/**
	 * 获取动态服务操作对象
	 * 如果服务不存在,会请求本地创建对应动态服务(服务只在当前jvm中创建)
	 */
	<T> CompletableFuture<T> serviceOfDynamicLocally(Class<T> ic, String sequence);

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