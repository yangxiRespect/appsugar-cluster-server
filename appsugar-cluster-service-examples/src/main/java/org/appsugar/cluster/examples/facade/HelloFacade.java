package org.appsugar.cluster.examples.facade;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.examples.domain.HelloRequest;
import org.appsugar.cluster.service.annotation.Service;

@Service(HelloFacade.name)
public interface HelloFacade {
	public static final String name = "hello";

	public CompletableFuture<String> sayHello(HelloRequest req);

	public CompletableFuture<String> sayHello(String req);

}
