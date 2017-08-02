package org.appsugar.microservice;

import java.io.File;
import java.util.Optional;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 启动器
 * @author NewYoung
 * 2017年3月22日下午3:34:46
 */
@Configuration
@EnableAutoConfiguration
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	@SuppressWarnings("unused")
	public static void main(String... args) throws Exception {
		Config config = ConfigFactory.load();
		Optional<Config> localConfig = loadConfig("./application.conf");
		if (localConfig.isPresent()) {
			config = localConfig.get().resolve().withFallback(config);
		}
		ServiceClusterSystem clusterSystem = new AkkaServiceClusterSystem("c", config);
		new DistributionRPCSystemImpl(clusterSystem);
		SpringApplication.run(Application.class, args);
	}

	public static Optional<Config> loadConfig(String file) {
		File f = new File(file);
		if (f.isFile()) {
			logger.info("load local config from path {}", f.getAbsolutePath());
			return Optional.of(ConfigFactory.parseFile(f));
		}

		logger.info("local config {} does not exist", f.getAbsolutePath());
		return Optional.empty();
	}
}