package org.appsugar.cluster.service.akka.system;

import java.util.ArrayList;
import java.util.List;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import junit.framework.TestCase;

public class AkkaServiceClusterSystemTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(AkkaServiceClusterSystemTest.class);

	@Test
	public void testSingleSystem() {
		ServiceClusterSystem system = new AkkaServiceClusterSystem("system", ConfigFactory.load("application.conf"));
		ServiceRef serviceRef = system.serviceFor(new SayHelloService(), "hello");
		String msg = serviceRef.ask("xx");
		ServiceClusterRef clusterRef = system.serviceOf("hello");
		String msg1 = clusterRef.one().ask("xxx");
		logger.debug("service return msg is {} msg 2 is {}", msg, msg1);
		system.serviceFor(new SayHelloService(), "hello");
		String msg2 = clusterRef.balance().ask("1");
		String msg3 = clusterRef.random().ask("1");
		logger.debug(" cluster Ref balance {} random {} ", msg2, msg3);
	}

	@Test
	public void testServiceFor() {
		ServiceClusterSystem system = new AkkaServiceClusterSystem("system", ConfigFactory.load("application.conf"));
		Service service = new SayHelloService();
		system.serviceFor(service, "x");
		try {
			system.serviceFor(service, "x");
			Assert.fail("same service register twice");
		} catch (Exception ex) {
			//DO NOTHING
		}
	}

	@Test
	public void testMultipleSystem() throws Exception {
		//初始化五个
		int[] prots = { 2551, 0, 0, 0, 0 };
		String systemName = "ClusterSystem";
		List<ServiceClusterSystem> systemList = new ArrayList<ServiceClusterSystem>();
		ServiceClusterSystem first = null;
		for (int port : prots) {
			Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
					.withFallback(ConfigFactory.load("application.conf"));
			ServiceClusterSystem system = new AkkaServiceClusterSystem(systemName, config);
			if (first == null) {
				first = system;
			}
			systemList.add(system);
			system.serviceFor(new SayHelloService(), "hello");
		}
		Thread.sleep(6000);
		ServiceClusterRef clusterRef = first.serviceOf("hello");
		logger.debug("cluster ref is {}", clusterRef);
		systemList.stream().forEachOrdered(s -> s.terminate());
	}

}
