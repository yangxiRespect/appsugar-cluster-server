package org.appsugar.cluster.service.akka.system;

import java.util.Map;

import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;

public class AkkaServiceContext implements ServiceContext {

	private AkkaServiceRef self;
	private AkkaServiceRef sender;
	private AkkaServiceClusterSystem system;

	@Override
	public ServiceRef self() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRef sender() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceClusterSystem system() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAttribute(Object name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getAttribute(Object name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Object, Object> attributes() {
		// TODO Auto-generated method stub
		return null;
	}

}