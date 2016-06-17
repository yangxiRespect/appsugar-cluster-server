package org.appsugar.cluster.service.binding.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.appsugar.cluster.service.api.ServiceException;
import org.appsugar.cluster.service.binding.DistributionRPCSystem;
import org.appsugar.cluster.service.binding.RPCSystemUtil;
import org.appsugar.cluster.service.binding.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * 服务定义
 * 把服务注册到远程调用系统上
 * @author NewYoung
 * 2016年6月17日上午11:21:35
 */
@Lazy(false)
public class ServiceDefination {

	private static Logger logger = LoggerFactory.getLogger(ServiceDefination.class);

	private DistributionRPCSystem system;

	private List<Object> serves;

	public DistributionRPCSystem getSystem() {
		return system;
	}

	public List<Object> getServes() {
		return serves;
	}

	@Autowired
	public void setSystem(DistributionRPCSystem system) {
		this.system = system;
	}

	@Autowired
	public void setServes(List<Object> serves) {
		this.serves = serves;
	}

	@PostConstruct
	public void init() {
		Map<Class<?>, Object> servesMap = new HashMap<>();
		serves.stream().forEach(e -> servesMap.put(getServeInterface(e), e));
		Map<String, Map<Class<?>, Object>> groupByServiceName = servesMap.entrySet().stream()
				.collect(Collectors.groupingBy(e -> RPCSystemUtil.getServiceName(e.getKey()),
						Collectors.toMap(Entry::getKey, Entry::getValue)));
		groupByServiceName.entrySet().stream().forEach(e -> system.serviceFor(e.getValue(), e.getKey()));

	}

	protected Class<?> getServeInterface(Object target) {
		Class<?>[] clazz = target.getClass().getInterfaces();
		for (Class<?> c : clazz) {
			if (!c.isAnnotationPresent(Service.class)) {
				continue;
			}
			return c;
		}
		throw new ServiceException(
				"Did not found Service Interface annotated with org.appsugar.cluster.service.binding.annotation.Service");
	}
}
