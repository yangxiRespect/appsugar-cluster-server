package org.appsugar.cluster.service.binding.spring;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.annotation.Service;

/**
 * 简单演示服务
 * @author NewYoung
 * 2016年6月17日下午1:36:20
 */
@Service(SampleService.name)
public interface SampleService {
	public static final String name = "sample";

	public CompletableFuture<String> areYouOk(String whoAsk);
}
