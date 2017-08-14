package org.appsugar.cluster.examples;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.appsugar.cluster.examples.facade.GatewayFacade;
import org.appsugar.cluster.examples.facade.HelloFacade;
import org.appsugar.cluster.examples.facade.factory.GatewayFacadeFactory;
import org.appsugar.cluster.examples.facade.impl.HelloFacadeImpl;
import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.appsugar.cluster.service.domain.ServiceDescriptor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Application {
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load();
		Optional<Config> localConfig = loadConfig("./application.conf");
		if (localConfig.isPresent()) {
			config = localConfig.get().resolve().withFallback(config);
		}
		ServiceClusterSystem clusterSystem = new AkkaServiceClusterSystem("c", config);
		DistributionRPCSystem system = new DistributionRPCSystemImpl(clusterSystem);
		//注册一个服务
		system.serviceFor(new ServiceDescriptor(Arrays.asList(new HelloFacadeImpl())), HelloFacade.name);
		//消费者依赖服务10000
		system.require(HelloFacade.class); // or system.require(HelloFacade.name);
		HelloFacade facade = system.serviceOf(HelloFacade.class);
		facade.sayHello("hello").thenAccept(System.out::println);
		//注册一个动态服务工厂
		system.registerFactory(new GatewayFacadeFactory());
		system.require(GatewayFacade.name);
		Thread.sleep(2000);
		GatewayFacade f = system.serviceOfDynamic(GatewayFacade.class, "0").get();
		f.tell("oo1", "hello oo1").get();
		Thread.sleep(6000);
		//关闭本地服务
		system.stop(f);
		Thread.sleep(100000);
	}

	public static Optional<Config> loadConfig(String file) {
		File f = new File(file);
		if (f.isFile()) {
			return Optional.of(ConfigFactory.parseFile(f));
		}

		return Optional.empty();
	}
}
