package org.appsugar.cluster.service.binding.spring;

import org.appsugar.BaseSpringTest;
import org.appsugar.cluster.service.binding.DistributionRPCSystem;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author NewYoung
 * 2016年6月16日下午5:52:03
 */
public class DistributionRPCSystemCreatorTest extends BaseSpringTest {

	@Autowired
	private DistributionRPCSystem system;

	@Test
	public void testSystemCreator() {
		Assert.assertNotNull(system);
	}

}
