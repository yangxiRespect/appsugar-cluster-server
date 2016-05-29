package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.Service;

public class ServiceInvokeProcessor implements MessageProcessor {

	private Service service;
	private AkkaServiceContext ctx;
	private AkkaServiceClusterSystem system;

	@Override
	public Object process(ProcessorContext ctx, Object msg) {
		// TODO Auto-generated method stub
		return null;
	}

}