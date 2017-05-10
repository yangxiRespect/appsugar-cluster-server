package org.appsugar.cluster.service.binding.spring;

import org.springframework.transaction.annotation.Transactional;

import function.ThrowableSupplier;

/**
 * spring事物
 * @author NewYoung
 * 2016年7月2日上午9:09:05
 */
public class TransactionalExecutor {

	@Transactional(readOnly = true)
	public <T> T readOnly(ThrowableSupplier<T> supplier) throws Throwable {
		return supplier.get();
	}

	@Transactional
	public <T> T required(ThrowableSupplier<T> supplier) throws Throwable {
		return supplier.get();
	}
}
