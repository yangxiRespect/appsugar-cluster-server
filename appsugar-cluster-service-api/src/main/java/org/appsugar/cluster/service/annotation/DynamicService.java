package org.appsugar.cluster.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态服务注解
 * @author NewYoung
 * 2016年6月14日下午1:52:33
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface DynamicService {
	/**
	 * 获取动态服务名称
	 */
	String value();

}
