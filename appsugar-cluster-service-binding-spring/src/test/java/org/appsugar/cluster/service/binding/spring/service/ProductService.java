package org.appsugar.cluster.service.binding.spring.service;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.binding.annotation.Service;

@Service(ProductService.name)
public interface ProductService {
	public static final String name = "product";

	public CompletableFuture<String> getProductName();
}
