package org.appsugar.cluster.examples;

import java.io.File;
import java.util.Optional;

import org.appsugar.cluster.examples.facade.GatewayFacade;
import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;

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
		//注册一个动态服务工厂
		//system.registerFactory(new GatewayFacadeFactory());
		Thread.sleep(4000);
		system.serviceOfDynamic(GatewayFacade.class, "1").tell("xxx", "I love u");
		Thread.sleep(20000);
		system.serviceOfDynamic(GatewayFacade.class, "1").tell("xxx", "I love u");
	}

	public static Optional<Config> loadConfig(String file) {
		File f = new File(file);
		if (f.isFile()) {
			return Optional.of(ConfigFactory.parseFile(f));
		}

		return Optional.empty();
	}
}
