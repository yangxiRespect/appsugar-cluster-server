package org.appsugar.cluster.service.akka.share;

import akka.actor.UntypedActor;

public class TestActor extends UntypedActor {

	private int i = 0;

	@Override
	public void onReceive(Object msg) throws Exception {
		i++;
	}

}
