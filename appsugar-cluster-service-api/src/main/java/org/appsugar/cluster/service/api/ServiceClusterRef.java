package org.appsugar.cluster.service.api;

/**
 * 服务集群引用
 * @author NewYoung
 * 2016年5月23日下午1:50:44
 */
public interface ServiceClusterRef {

	/**
	 * 获取服务名称 
	 */
	String name();

	/**
	 * 获取服务个数
	 */
	int size();

	/**
	 * 获取所有服务
	 */
	Iterable<ServiceRef> iterable();

	/**
	 * 随机获取一个服务引用,当服务数为零时返回空
	 */
	ServiceRef random();

	/**
	 * 采取均衡方式获取服务引用,当服务数为零时返回空
	 */
	ServiceRef balance();

	/**
	 * 根据seed % serviceRef.length 获取serviceRef
	 */
	ServiceRef balance(int seed);

	/**
	 *获取一个引用,当服务数为零时返回空
	 *优先本地引用
	 */
	ServiceRef one();

	/**
	 * 获取一个路径最小的引用
	 * 使用路径和名称做比较
	 */
	ServiceRef leader();

}