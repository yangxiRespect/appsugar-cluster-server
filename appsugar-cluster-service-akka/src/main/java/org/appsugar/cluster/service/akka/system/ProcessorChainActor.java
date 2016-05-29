package org.appsugar.cluster.service.akka.system;

import akka.actor.UntypedActor;

public class ProcessorChainActor extends UntypedActor {

	private MessageProcessorChain chain;

	private ActorContext ctx;

	@Override
	public void onReceive(Object arg0) throws Exception {

	}

}