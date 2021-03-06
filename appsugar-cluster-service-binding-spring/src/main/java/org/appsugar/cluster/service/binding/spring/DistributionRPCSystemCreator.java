package org.appsugar.cluster.service.binding.spring;

import java.io.File;
import java.util.Objects;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 分布式方法调用系统创建器
 * @author NewYoung
 * 2016年6月16日下午5:27:14
 */
public class DistributionRPCSystemCreator implements FactoryBean<DistributionRPCSystem> {

	private static final Logger logger = LoggerFactory.getLogger(DistributionRPCSystemCreator.class);

	private DistributionRPCSystem system;

	private String configs;

	private String name;

	private Environment env;

	protected DistributionRPCSystem create() {
		Config config = ConfigFactory.load();
		if (configs != null) {
			String[] configArray = configs.split(",");
			for (int i = 0; i < configArray.length; i++) {
				String resource = configArray[i];
				try {
					File file = new File(resource);
					logger.debug("load akka resource {}  is local file {}", resource, file.isFile());
					Config resourceConfig = file.isFile() ? ConfigFactory.parseFile(file)
							: ConfigFactory.parseResources(resource);
					config = resourceConfig.resolve().withFallback(config);
				} catch (Exception ex) {
					logger.warn("akka config resource not found {} ", resource, ex);
				}
			}
		}
		String seedNodes = env.getProperty("akka.cluster.seed-nodes");
		if (Objects.nonNull(seedNodes)) {
			logger.debug("set seed nodes from spring cloud config {}", seedNodes);
			config = ConfigFactory.parseString("akka.cluster.seed-nodes=" + seedNodes).withFallback(config);
		}
		system = new DistributionRPCSystemImpl(new AkkaServiceClusterSystem(name, config));
		return system;
	}

	public void terminate() {
		system.terminate();
	}

	@Override
	public DistributionRPCSystem getObject() throws Exception {
		if (system != null) {
			return system;
		}
		return create();
	}

	@Override
	public Class<?> getObjectType() {
		return DistributionRPCSystem.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getConfigs() {
		return configs;
	}

	public void setConfigs(String configs) {
		this.configs = configs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Environment getEnv() {
		return env;
	}

	@Autowired
	public void setEnv(Environment env) {
		this.env = env;
	}

}
