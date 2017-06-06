package org.appsugar.cluster.examples;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import org.appsugar.cluster.examples.facade.HelloFacade;
import org.appsugar.cluster.examples.facade.impl.HelloFacadeImpl;
import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.appsugar.cluster.service.domain.ServiceDescriptor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Application {
	public static void main(String[] args) {
		Config config = ConfigFactory.load();
		Optional<Config> localConfig = loadConfig("./application.conf");
		if (localConfig.isPresent()) {
			config = localConfig.get().resolve().withFallback(config);
		}
		ServiceClusterSystem clusterSystem = new AkkaServiceClusterSystem("c", config);
		DistributionRPCSystem system = new DistributionRPCSystemImpl(clusterSystem);
		//注册一个服务
		system.serviceFor(new ServiceDescriptor(Arrays.asList(new HelloFacadeImpl())), HelloFacade.name);
		//消费者依赖服务
		system.require(HelloFacade.class); // or system.require(HelloFacade.name);
		HelloFacade facade = system.serviceOf(HelloFacade.class);
		facade.sayHello("hello").thenAccept(System.out::println);
	}

	public static Optional<Config> loadConfig(String file) {
		File f = new File(file);
		if (f.isFile()) {
			return Optional.of(ConfigFactory.parseFile(f));
		}

		return Optional.empty();
	}
}
