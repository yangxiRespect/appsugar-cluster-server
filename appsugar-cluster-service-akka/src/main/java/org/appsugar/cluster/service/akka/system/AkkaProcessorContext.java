package org.appsugar.cluster.service.akka.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * akka处理上下文
 * @author NewYoung
 * 2016年5月29日下午5:37:57
 */
public class AkkaProcessorContext implements ProcessorContext {

	private ActorContext ctx;
	private AkkaProcessorContext next;
	private MessageProcessor processor;

	public AkkaProcessorContext(ActorContext ctx, MessageProcessor processor) {
		super();
		this.ctx = ctx;
		this.processor = processor;
	}

	@Override
	public ActorRef getSelf() {
		return ctx.self();
	}

	@Override
	public ActorRef getSender() {
		return ctx.sender();
	}

	@Override
	public ActorSystem getSystem() {
		return ctx.system();
	}

	@Override
	public Object processNext(Object msg) throws Throwable {
		if (next == null) {
			return null;
		}
		return next.processor.process(next, msg);
	}

	void setNext(AkkaProcessorContext next) {
		this.next = next;
	}
}