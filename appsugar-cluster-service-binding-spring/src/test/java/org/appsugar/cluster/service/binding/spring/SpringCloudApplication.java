package org.appsugar.cluster.service.binding.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class SpringCloudApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication
				.run(new Object[] { SpringCloudApplication.class, DistributionRPCSystemAutoConfiguration.class }, args);
		context.close();
	}
}
