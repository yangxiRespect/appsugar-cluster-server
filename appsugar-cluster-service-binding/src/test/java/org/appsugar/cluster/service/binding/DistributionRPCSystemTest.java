package org.appsugar.cluster.service.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.annotation.DynamicService;
import org.appsugar.cluster.service.annotation.ExecuteDefault;
import org.appsugar.cluster.service.annotation.ExecuteOnEvent;
import org.appsugar.cluster.service.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.annotation.Service;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.domain.ServiceDescriptor;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.util.RPCSystemUtil;
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
		system.serviceFor(new ServiceDescriptor(Arrays.asList(new HelloImpl())), "test");
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
		String dynamicServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence("productCreate", sequence);
		Assert.assertNotNull(system.serviceOfDynamicIfPresent(ProductOperationService.class, sequence));
		system.stop(dynamicServiceName);
		Thread.sleep(2000);
		Assert.assertNull(system.serviceOfDynamicIfPresent(ProductOperationService.class, sequence));
		system.terminate();
	}

	public void testBinding() throws Exception {
		Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2551)
				.withFallback(ConfigFactory.load());
		DistributionRPCSystem system = new DistributionRPCSystemImpl(
				new AkkaServiceClusterSystem("ClusterSystem", config));
		system.serviceFor(new ServiceDescriptor(Arrays.asList(new HelloImpl())), Hello.serviceName);
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
	public CompletableFuture<ServiceDescriptor> create(String sequence) {
		List<Object> serves = new ArrayList<>();
		serves.add(new ProductOperationServiceImpl(Long.parseLong(sequence)));
		ServiceDescriptor descriptor = new ServiceDescriptor(serves, false);
		return CompletableFuture.completedFuture(descriptor);
	}

	@Override
	public String name() {
		return "productCreate";
	}

	@Override
	public void init(DistributionRPCSystem system) {
		System.out.println("dynaimc create service  init");
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