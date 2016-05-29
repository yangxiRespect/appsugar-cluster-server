package org.appsugar.cluster.service.akka.system;

import akka.actor.ActorRef;

public class AkkaProcessorContext implements ProcessorContext {

	private ActorContext ctx;
	private AkkaProcessorContext next;
	private MessageProcessor processor;

	@Override
	public ActorRef getSelf() {
		return null;
	}

	@Override
	public ActorRef getSender() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActorRef getSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object process(Object msg) {
		// TODO Auto-generated method stub
		return null;
	}

}