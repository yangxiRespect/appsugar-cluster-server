package org.appsugar.cluster.service.akka.system;

import akka.actor.*;

public interface ProcessorContext {

	/**
	 * 
	 * @return 
	 */
	ActorRef getSelf();

	/**
	 * 
	 * @return 
	 */
	ActorRef getSender();

	/**
	 * 
	 * @return 
	 */
	ActorRef getSystem();

	/**
	 * 
	 * @param msg
	 * @return 
	 */
	Object process(Object msg);

}