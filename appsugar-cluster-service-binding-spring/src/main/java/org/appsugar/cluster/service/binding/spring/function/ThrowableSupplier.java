package org.appsugar.cluster.service.binding.spring.function;

/**
 * 可抛异常的提供者
 * @author NewYoung
 * 2017年5月2日下午3:34:18
 * @param <T>
 */
@FunctionalInterface
public interface ThrowableSupplier<T> {

	T get() throws Throwable;

}
