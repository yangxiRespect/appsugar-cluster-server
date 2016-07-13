package org.appsugar.cluster.service.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.api.Status;
import org.appsugar.cluster.service.api.annotation.DynamicService;
import org.appsugar.cluster.service.api.annotation.ExecuteDefault;
import org.appsugar.cluster.service.api.annotation.ExecuteOnEvent;
import org.appsugar.cluster.service.api.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.api.annotation.Service;
import org.junit.Assert;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import junit.framework.TestCase;

public class DistributionRPCSystemTest extends TestCase {

	@Test
	public void testInvoke() throws Exception {
		DistributionRPCSystem system = new DistributionRPCSystemImpl(
				new AkkaServiceClusterSystem("a", ConfigFactory.load()));
		Map<Class<?>, Object> serves = new HashMap<>();
		serves.put(Hello.class, new HelloImpl());
		system.serviceFor(serves, "test");
		Hello h = system.serviceOf(Hello.class);
		System.out.println(" 调用第一次结果" + h.sayHello());
		System.out.println(" 调用第二次结果" + h.sayHello());
		System.out.println(" async 调用结果" + h.asyncSayHello().get());
		system.publish("1String", "play");
		system.publish(1, "play");
		Thread.sleep(1000);
		system.terminate();
	}

	@Test
	public void testDynamic() throws Exception {
		DistributionRPCSystem system = new DistributionRPCSystemImpl(
				new AkkaServiceClusterSystem("a", ConfigFactory.load()));
		system.registerFactory(new ProductOperationServiceServiceFactory());
		String sequence = "1";
		ProductOperationService p = system.serviceOfDynamic(ProductOperationService.class, sequence);
		System.out.println(" id is " + p.id());
		Assert.assertEquals(p, system.serviceOfDynamic(ProductOperationService.class, sequence));
		system.terminate();
	}

	public void testBinding() throws Exception {
		Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2551)
				.withFallback(ConfigFactory.load());
		DistributionRPCSystem system = new DistributionRPCSystemImpl(
				new AkkaServiceClusterSystem("ClusterSystem", config));
		Map<Class<?>, Object> serves = new HashMap<>();
		serves.put(Hello.class, new HelloImpl());
		system.serviceFor(serves, Hello.serviceName);
		Thread.sleep(8000);
	}

	public void testRemoteInvoke() throws Exception {
		DistributionRPCSystem system = new DistributionRPCSystemImpl(
				new AkkaServiceClusterSystem("ClusterSystem", ConfigFactory.load()));
		Hello hello = system.serviceOf(Hello.class);
		Thread.sleep(3000);
		String sayHello = hello.sayHello();
		System.out.println("sayHello " + sayHello);
		Thread.sleep(2000);
	}

}

@DynamicService("productCreate")
interface ProductOperationService {

	public Long id();

}

class ProductOperationServiceImpl implements ProductOperationService {

	private Long id;

	public ProductOperationServiceImpl(Long id) {
		super();
		this.id = id;
	}

	@Override
	public Long id() {
		return id;
	}

}

class ProductOperationServiceServiceFactory implements DynamicServiceFactory {

	@Override
	public CompletableFuture<Map<Class<?>, ?>> create(String sequence) {
		Map<Class<?>, Object> result = new HashMap<>();
		result.put(ProductOperationService.class, new ProductOperationServiceImpl(Long.parseLong(sequence)));
		return CompletableFuture.completedFuture(result);
	}

	@Override
	public String service() {
		return "productCreate";
	}

}

@Service(Hello.serviceName)
interface Hello extends Hello1 {
	public static final String serviceName = "test";

	public String sayHello();

	public CompletableFuture<String> asyncSayHello();
}

interface Hello1 {

}

class HelloImpl implements Hello {
	int i = 0;

	@Override
	public String sayHello() {
		return "hello" + (i++);
	}

	@Override
	public CompletableFuture<String> asyncSayHello() {
		return CompletableFuture.completedFuture("xxx");
	}

	@ExecuteOnServiceReady
	public void setHello(Hello hello, Status status) {
		System.out.println("1234 " + status);
	}

	@ExecuteDefault
	public void init() {
		System.out.println("init ");
	}

	@ExecuteOnEvent("play")
	public void event(String a) {
		System.out.println("event " + a);
	}

	@ExecuteOnEvent("play")
	public void event1(Integer a) {
		System.out.println("event " + a);
	}
}