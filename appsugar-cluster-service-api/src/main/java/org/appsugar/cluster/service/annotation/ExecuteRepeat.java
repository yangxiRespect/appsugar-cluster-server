package org.appsugar.cluster.service.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重复调用
 * 方法搜索范围(当前类:不包含父类中方法)
 * @author NewYoung
 * 2016年6月3日上午2:57:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface ExecuteRepeat {
	/**
	 * 间隔(毫秒)
	 */
	public long value() default 1000l;
}