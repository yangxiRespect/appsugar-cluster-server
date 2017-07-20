package org.appsugar.cluster.examples.facade.impl;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.examples.domain.HelloRequest;
import org.appsugar.cluster.examples.facade.HelloFacade;

public class HelloFacadeImpl implements HelloFacade {

	@Override
	public CompletableFuture<String> sayHello(HelloRequest req) {
		return CompletableFuture.completedFuture("hello " + req.sender);
	}

	@Override
	public CompletableFuture<String> sayHello(String req) {
		return CompletableFuture.completedFuture("Nice to meet you ");
	}

}
