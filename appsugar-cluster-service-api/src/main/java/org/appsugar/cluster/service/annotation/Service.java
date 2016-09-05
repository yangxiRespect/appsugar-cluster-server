package org.appsugar.cluster.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记当前接口为服务
 * @author NewYoung
 * 2016年6月3日上午2:58:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface Service {
	/**
	 * 服务名称
	 */
	String value();
}