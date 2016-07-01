package org.appsugar.cluster.service.api.annotation;

/**
 * 调用超时设置
 * @author NewYoung
 * 2016年7月1日上午10:10:11
 */
public @interface ExecuteTimeout {
	long value() default 30000;
}
