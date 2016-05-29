package org.appsugar.cluster.service.akka.system;

import akka.actor.*;

public class ActorContext {

	private ActorRef selfe;
	private ActorRef system;
	private ActorRef sender;

	/**
	 * 
	 * @return 
	 */
	public ActorRef self() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return 
	 */
	public ActorRef sender() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return 
	 */
	public ActorSystem system() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param s
	 * @return 
	 */
	public void sender(ActorRef s) {
		throw new UnsupportedOperationException();
	}

}