package org.appsugar.cluster.service.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 调用超时设置
 * @author NewYoung
 * 2016年7月1日上午10:10:11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface ExecuteTimeout {
	long value() default 30000;
}
