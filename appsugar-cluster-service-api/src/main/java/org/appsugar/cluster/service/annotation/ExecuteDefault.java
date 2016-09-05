package org.appsugar.cluster.service.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 初始化完成后的,默认执行方法
 * 方法搜索范围(当前类:不包含父类中方法)
 * @author NewYoung
 * 2016年6月3日上午2:42:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface ExecuteDefault {
}