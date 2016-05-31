package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceContext;

/**
 * 
 * @author NewYoung
 * 2016年5月31日上午10:28:44
 */
public class SayHelloService implements Service {

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		return "hello my name is jone@" + hashCode();
	}

}
