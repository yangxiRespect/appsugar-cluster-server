package org.appsugar.cluster.service.binding.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当topic事件发生时
 * 方法搜索范围(当前类:不包含父类中方法)
 * @author NewYoung
 * 2016年6月3日上午2:46:51
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface ExecuteOnEvent {

	/**
	 * 关注的topic
	 */
	String value();

}