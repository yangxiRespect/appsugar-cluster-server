package org.appsugar.cluster.service.binding.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.appsugar.cluster.service.annotation.Service;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.domain.ServiceDescriptor;
import org.appsugar.cluster.service.domain.ServiceException;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.Lazy;

/**
 * 服务定义
 * 把服务注册到远程调用系统上
 * @author NewYoung
 * 2016年6月17日上午11:21:35
 */
@Lazy(false)
public class ServiceDefination {

	private DistributionRPCSystem system;

	private List<Object> serves;

	public DistributionRPCSystem getSystem() {
		return system;
	}

	public List<Object> getServes() {
		return serves;
	}

	public void setSystem(DistributionRPCSystem system) {
		this.system = system;
	}

	public void setServes(List<Object> serves) {
		this.serves = serves;
	}

	@PostConstruct
	public void init() {
		Map<Class<?>, Object> servesMap = new HashMap<>();
		//处理spring代理对象
		serves.stream().forEach(e -> {
			Object target = e;
			if (AopUtils.isAopProxy(target)) {
				target = new SpringProxyServer(target);
			}
			servesMap.put(getServeInterface(e), target);
		});
		servesMap.entrySet().stream()
				.collect(Collectors.groupingBy(e -> RPCSystemUtil.getServiceName(e.getKey()),
						Collectors.mapping(e1 -> e1.getValue(), Collectors.toList())))
				.entrySet().stream()
				.forEach(e -> system.serviceForAsync(new ServiceDescriptor(e.getValue()), e.getKey()));

	}

	protected Class<?> getServeInterface(Object target) {
		Class<?> clazz = AopUtils.getTargetClass(target);
		do {
			for (Class<?> c : clazz.getInterfaces()) {
				if (!c.isAnnotationPresent(Service.class)) {
					continue;
				}
				return c;
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null);
		throw new ServiceException(
				"Did not found Service Interface annotated with org.appsugar.cluster.service.binding.annotation.Service "
						+ target.getClass());
	}
}
