package org.appsugar.cluster.service.akka.system;

import akka.actor.ActorRef;

public class ResponseOnlyServiceRef extends AkkaServiceRef {

	public ResponseOnlyServiceRef(ActorRef ref, String name) {
		super(ref, name);
	}
}