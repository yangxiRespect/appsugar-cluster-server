package org.appsugar.cluster.service.binding.spring;

import java.util.function.Supplier;

import org.springframework.transaction.annotation.Transactional;

/**
 * spring事物
 * @author NewYoung
 * 2016年7月2日上午9:09:05
 */
public class TransactionalExecutor {

	@Transactional(readOnly = true)
	public <T> T readOnly(Supplier<T> supplier) {
		return supplier.get();
	}

	@Transactional
	public <T> T required(Supplier<T> supplier) {
		return supplier.get();
	}
}
