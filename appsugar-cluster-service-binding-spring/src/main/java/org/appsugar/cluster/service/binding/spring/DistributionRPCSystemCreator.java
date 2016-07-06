package org.appsugar.cluster.service.binding.spring;

import java.io.File;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

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

	protected DistributionRPCSystem create() {
		Config config = ConfigFactory.load();
		if (configs != null) {
			String[] configArray = configs.split(",");
			for (int i = 1; i < configArray.length; i++) {
				String resource = configArray[i];
				try {
					File file = new File(resource);
					Config resourceConfig = file.isFile() ? ConfigFactory.parseFile(file)
							: ConfigFactory.parseResources(resource);
					config = resourceConfig.withFallback(config);
				} catch (Exception ex) {
					logger.error("akka config resource not found {} ({})", resource, ex.getMessage());
				}
			}
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

}
