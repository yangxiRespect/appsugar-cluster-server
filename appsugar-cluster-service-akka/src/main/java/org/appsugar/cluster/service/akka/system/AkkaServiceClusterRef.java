package org.appsugar.cluster.service.akka.system;

import java.util.List;

import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceRef;

public class AkkaServiceClusterRef implements ServiceClusterRef {

	private List<AkkaServiceRef> serviceRefList;

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<ServiceRef> iterable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRef random() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRef balance() {
		// TODO Auto-generated method stub
		return null;
	}

}