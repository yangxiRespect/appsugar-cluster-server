package org.appsugar.cluster.service.binding.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.annotation.Service;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.binding.DistributionServiceInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 
 * 
 * 自动配置分布式远程调用系统
 * @author NewYoung
 *
 */
@Configurable
public class DistributionRPCSystemAutoConfiguration
		implements BeanPostProcessor, Ordered, ApplicationListener<ContextRefreshedEvent> {
	public static final String DEFAULT_DISTRIBUTION_THREAD_POOL_SIZE = "50";
	public static final String DISTRIBUTION_RPC_SYSTEM_NAME_KEY = "spring.appsugar.rpc.name";
	public static final String DISTRIBUTION_RPC_SYSTEM_CONFIG_KEY = "spring.appsugar.rpc.config";
	public static final String DISTRIBUTION_THREAD_POOL_MAX = "spring.appsugar.thread.pool.max";
	public static final String DISTRIBUTION_THREAD_POOL_DEFAULT = "spring.appsugar.thread.pool.default";
	private static final Logger logger = LoggerFactory.getLogger(DistributionRPCSystemAutoConfiguration.class);

	private List<DynamicServiceFactory> factoryList = new ArrayList<>();
	private List<Object> serviceList = new ArrayList<>();
	private AtomicBoolean registerFlag = new AtomicBoolean();

	@Autowired
	private Environment env;
	@Autowired
	private DistributionRPCSystem system;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof DynamicServiceFactory) {
			factoryList.add((DynamicServiceFactory) bean);
			logger.info("detected DynamicServiceFactory {}", bean);
		} else if (!(bean instanceof DistributionServiceInvocation)
				&& Objects.nonNull(AnnotationUtils.findAnnotation(AopUtils.getTargetClass(bean), Service.class))) {
			serviceList.add(bean);
			logger.info("detected Service  {}", bean);
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	@Bean
	public DistributionRPCSystem distributionRPCSystem() throws Exception {
		DistributionRPCSystemCreator creator = new DistributionRPCSystemCreator();
		creator.setEnv(env);
		creator.setConfigs(env.getProperty(DISTRIBUTION_RPC_SYSTEM_CONFIG_KEY, "application.conf"));
		creator.setName(env.getProperty(DISTRIBUTION_RPC_SYSTEM_NAME_KEY, "c"));
		return creator.getObject();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//make sure init only once
		if (!registerFlag.compareAndSet(false, true)) {
			return;
		}
		logger.info("************ContextStartedEvent Prepar to config RPCSystem**************");
		//注册动态服务工厂
		logger.info("prepar to register factory {}", factoryList);
		factoryList.stream().forEach(e -> system.registerFactory(e));
		//分组服务
		Map<Service, List<Object>> serves = serviceList.stream().collect(
				Collectors.groupingBy(e -> AnnotationUtils.findAnnotation(AopUtils.getTargetClass(e), Service.class)));
		serves.values().stream().forEach(e -> {
			logger.info("prepar to define rpc service {}", e);
			ServiceDefination defination = new ServiceDefination();
			defination.setSystem(system);
			defination.setServes(e);
			defination.init();
		});
	}

	@Bean
	public AsyncExecutor asyncExecutor(ThreadPoolTaskExecutor t, TransactionalExecutor te) {
		AsyncExecutor result = new AsyncExecutor();
		result.setExecutor(t);
		result.setTransactionalExecutor(te);
		return result;
	}

	@Bean
	public TransactionalExecutor transactionExecutor() {
		return new TransactionalExecutor();
	}

	@Bean
	public ThreadPoolTaskExecutor executor() {
		ThreadPoolTaskExecutor result = new ThreadPoolTaskExecutor();
		String maxPoolSize = env.getProperty(DISTRIBUTION_THREAD_POOL_MAX, DEFAULT_DISTRIBUTION_THREAD_POOL_SIZE);
		int size = Integer.parseInt(
				env.getProperty(DISTRIBUTION_THREAD_POOL_DEFAULT, Runtime.getRuntime().availableProcessors() + ""));
		int maxSize = Integer.parseInt(maxPoolSize);
		logger.debug("init thread pool task executor size is {} max size is {}", size, maxSize);
		result.setCorePoolSize(size);
		result.setMaxPoolSize(maxSize);
		return result;
	}
}
