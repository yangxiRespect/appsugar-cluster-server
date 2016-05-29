package org.appsugar.cluster.service.akka.system;

import java.util.List;

public class MessageProcessorChain {

	private List<MessageProcessor> processorList;

	private AkkaProcessorContext processorContextChain;

	/**
	 * 
	 * @param ctx
	 * @param msg
	 * @return 
	 */
	public void receive(ActorContext ctx, Object msg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param p
	 * @return 
	 */
	public void addLast(MessageProcessor p) {
		throw new UnsupportedOperationException();
	}

}