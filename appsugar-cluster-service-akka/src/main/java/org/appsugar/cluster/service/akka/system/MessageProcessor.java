package org.appsugar.cluster.service.akka.system;

public interface MessageProcessor {

	/**
	 * 
	 * @param ctx
	 * @param msg
	 * @return 
	 */
	Object process(ProcessorContext ctx, Object msg);

}