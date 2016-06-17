package org.appsugar.cluster.service.binding.spring.service;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;

public class ProductServiceImpl implements ProductService {

	@Override
	public CompletableFuture<String> getProductName() {
		ServiceContext context = ServiceContextThreadLocal.context();
		System.out.println("self name " + context.self().name() + " sender name " + context.sender().name());
		return CompletableFuture.completedFuture("纳米水晶");
	}

}
