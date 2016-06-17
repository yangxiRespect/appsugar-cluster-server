package org.appsugar.cluster.service.binding.spring;

import java.util.List;

import javax.annotation.PostConstruct;

import org.appsugar.cluster.service.binding.DistributionRPCSystem;
import org.appsugar.cluster.service.binding.DynamicServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * 动态服务创建工厂定义
 * @author NewYoung
 * 2016年6月17日上午11:04:20
 */
@Lazy(false)
public class DynamicServiceFactoryDefination {
	private static final Logger logger = LoggerFactory.getLogger(DynamicServiceFactoryDefination.class);

	private DistributionRPCSystem system;

	private List<DynamicServiceFactory> factoryList;

	public DistributionRPCSystem getSystem() {
		return system;
	}

	public List<DynamicServiceFactory> getFactoryList() {
		return factoryList;
	}

	@Autowired
	public void setSystem(DistributionRPCSystem system) {
		this.system = system;
	}

	@Autowired
	public void setFactoryList(List<DynamicServiceFactory> factoryList) {
		this.factoryList = factoryList;
	}

	@PostConstruct
	public void init() {
		if (system == null || factoryList == null) {
			return;
		}
		logger.debug("register DynamicServiceFactory {}", factoryList);
		factoryList.stream().forEach(e -> system.registerFactory(e));
	}
}
