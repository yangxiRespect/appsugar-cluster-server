package org.appsugar.cluster.service.akka.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;

/**
 * 消息链处理型actor
 * @author NewYoung
 * 2016年5月29日下午5:42:15
 */
public class ProcessorChainActor extends AbstractActor {
	private static final Logger logger = LoggerFactory.getLogger(ProcessorChainActor.class);

	private MessageProcessorChain chain;

	public ProcessorChainActor(MessageProcessorChain chain) {
		super();
		this.chain = chain;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchAny(msg -> {
			try {
				chain.receive(context(), msg);
			} catch (Throwable e) {
				logger.error("process msg error msg:{} ex: {}", msg, e);
			}
		}).build();
	}
}