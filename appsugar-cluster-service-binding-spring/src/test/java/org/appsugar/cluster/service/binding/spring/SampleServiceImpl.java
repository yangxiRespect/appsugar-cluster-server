package org.appsugar.cluster.service.binding.spring;

import org.appsugar.cluster.service.api.Status;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.binding.spring.service.ProductService;

public class SampleServiceImpl implements SampleService {

	private ProductService productService;

	@Override
	public String areYouOk(String whoAsk) {
		if ("雷军".equals(whoAsk)) {
			return "I am ok!";
		}
		productService.getProductName();
		return "I am not ok!";
	}

	@ExecuteOnServiceReady
	public void productService(ProductService productService, Status status) {
		this.productService = productService;
	}

}
