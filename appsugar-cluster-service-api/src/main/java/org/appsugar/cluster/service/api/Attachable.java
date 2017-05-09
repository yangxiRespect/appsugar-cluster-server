package org.appsugar.cluster.service.api;

/**
 * 附件接口
 * @author NewYoung
 * 2017年5月9日下午12:50:52
 */
public interface Attachable {

	/**
	 * 根据key获取数据
	 * 如果没有,那么返回默认值
	 * @author NewYoung
	 * 2017年5月9日下午12:55:10
	 */
	public <K, V> V getOrDefault(K key, V defaultValue);

	/**
	 * 根据key值获取附件信息
	 * @author NewYoung
	 * 2017年5月9日下午12:52:14
	 */
	public <K, V> V get(K key);

	/**
	 * 数据关联
	 * @author NewYoung
	 * 2017年5月9日下午12:52:29
	 */
	public <K, V> V attach(K key, V value);
}
