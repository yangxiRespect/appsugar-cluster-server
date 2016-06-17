package org.appsugar.cluster.service.binding.spring;

import org.appsugar.cluster.service.akka.system.AkkaServiceClusterSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystem;
import org.appsugar.cluster.service.binding.DistributionRPCSystemImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 分布式方法调用系统创建器
 * @author NewYoung
 * 2016年6月16日下午5:27:14
 */
public class DistributionRPCSystemCreator implements FactoryBean<DistributionRPCSystem> {

	private DistributionRPCSystem system;

	private String configs;

	private String name;

	private int port;

	protected DistributionRPCSystem create() {
		Config config = ConfigFactory.load();
		if (configs != null) {
			String[] configArray = configs.split(",");
			config = ConfigFactory.load(configArray[0]);
			for (int i = 1; i < configArray.length; i++) {
				config = config.withFallback(ConfigFactory.parseResources(configArray[i]));
			}
		}
		config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(config);
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

	@Autowired(required = true)
	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
