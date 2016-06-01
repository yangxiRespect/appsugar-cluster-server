package org.appsugar.cluster.service.akka.share;

import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import junit.framework.TestCase;

public class ConfigTest extends TestCase {

	@Test
	public void testConfigMerge() {
		Config defaultConfig = ConfigFactory.load();
		Config protostuffConfig = ConfigFactory.load("protostuff-mapping.conf");
		Config c = protostuffConfig.withFallback(defaultConfig);
		System.out.println(c);
		System.out.println(ConfigTest.class.getName().hashCode());
	}
}
