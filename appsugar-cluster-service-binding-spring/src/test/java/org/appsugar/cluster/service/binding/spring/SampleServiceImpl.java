package org.appsugar.cluster.service.binding.spring;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.binding.spring.service.ProductService;
import org.appsugar.cluster.service.domain.Status;

public class SampleServiceImpl implements SampleService {

	private ProductService productService;

	@Override
	public CompletableFuture<String> areYouOk(String whoAsk) {
		if ("雷军".equals(whoAsk)) {
			return CompletableFuture.completedFuture("I am ok");
		}
		return productService.getProductName();
	}

	@ExecuteOnServiceReady
	public void productService(ProductService productService, Status status) {
		this.productService = productService;
	}

}
