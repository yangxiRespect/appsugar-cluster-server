package org.appsugar.cluster.service.binding.spring;

import org.appsugar.BaseSpringTest;
import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceDefinationTest extends BaseSpringTest {

	private SampleService sampleService;

	@Test
	public void testAreYouOk() {
		String msg = sampleService.areYouOk("小李子");
		logger.debug("msg is {}", msg);
		msg = sampleService.areYouOk("雷军");
		logger.debug("msg is {}", msg);
	}

	@Autowired
	public void setDistributionRPCSystem(DistributionRPCSystem system) {
		sampleService = system.serviceOf(SampleService.class);
	}
}
