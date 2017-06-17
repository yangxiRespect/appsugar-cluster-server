package org.appsugar.cluster.examples.facade.factory;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.examples.facade.GatewayFacade;
import org.appsugar.cluster.examples.facade.impl.GatewayFacadeImpl;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.domain.ServiceDescriptor;

/**
 * 网关动态服务注册工厂
 * @author shenliuyang@gmail.com
 *
 */
public class GatewayFacadeFactory implements DynamicServiceFactory {

	@Override
	public CompletableFuture<ServiceDescriptor> create(String sequence) {
		System.out.println("create dynamic gatewayfacade  sequence is " + sequence);
		return CompletableFuture.completedFuture(new ServiceDescriptor(new GatewayFacadeImpl(sequence)));
	}

	@Override
	public String name() {
		return GatewayFacade.name;
	}

}
