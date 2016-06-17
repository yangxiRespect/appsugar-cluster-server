package org.appsugar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration({ "classpath:/applicationContext.xml" })
public abstract class BaseSpringTest extends AbstractJUnit4SpringContextTests {

	protected static final Logger logger = LoggerFactory.getLogger(BaseSpringTest.class);
}
