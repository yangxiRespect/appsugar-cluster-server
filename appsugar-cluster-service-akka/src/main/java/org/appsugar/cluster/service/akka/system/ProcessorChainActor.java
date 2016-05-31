package org.appsugar.cluster.service.akka.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

/**
 * 消息链处理型actor
 * @author NewYoung
 * 2016年5月29日下午5:42:15
 */
public class ProcessorChainActor extends UntypedActor {
	private static final Logger logger = LoggerFactory.getLogger(ProcessorChainActor.class);

	private MessageProcessorChain chain;

	private ActorContext ctx = new ActorContext(getSelf(), getContext().system());

	public ProcessorChainActor(MessageProcessorChain chain) {
		super();
		this.chain = chain;
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		ctx.sender(getSender());
		try {
			chain.receive(ctx, msg);
		} catch (Throwable e) {
			logger.error("process msg error msg:{} ex: {}", msg, e);
		}
	}

}