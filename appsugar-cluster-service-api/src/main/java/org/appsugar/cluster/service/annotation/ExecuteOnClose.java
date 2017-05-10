package org.appsugar.cluster.service.annotation;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务关闭执行
 * @author NewYoung
 * 2017年5月10日上午10:05:16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface ExecuteOnClose {

}
