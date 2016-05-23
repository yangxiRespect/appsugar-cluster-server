package org.appsugar.cluster.service.api;

import java.util.Map;

/**
 * 服务上下文,每一个service对应一个上下文
 * @author NewYoung
 * 2016年5月23日下午2:13:35
 */
public interface ServiceContext {

	/**
	 * 获取服务自己的引用
	 */
	ServiceRef self();

	/**
	 * 获取发送方引用.
	 * 注意:只有线程在服务中执行才能获取到发送方. 
	 * 发送方有可能为空
	 */
	ServiceRef sender();

	/**
	 * 获取对应服务集群系统 
	 */
	ServiceClusterSystem system();

	/**
	 * 添加属性如果属性存在,那么覆盖.
	 */
	void addAttribute(Object name, Object value);

	/**
	 * 根据名称获取属性值 
	 */
	Object getAttribute(Object name);

	/**
	 * 获取所有属性的map 
	 */
	Map<Object, Object> attributes();

}