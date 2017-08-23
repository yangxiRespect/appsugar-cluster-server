package org.appsugar.cluster.service.akka.system;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.appsugar.cluster.service.akka.domain.LocalPubSubMessage;
import org.appsugar.cluster.service.domain.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

/**
 * 消息链处理型actor
 * @author NewYoung
 * 2016年5月29日下午5:42:15
 */
public class ProcessorChainActor extends AbstractActor {
	private static final Logger logger = LoggerFactory.getLogger(ProcessorChainActor.class);

	private static ThreadLocal<ProcessorChainActor> local = new ThreadLocal<>();
	private Set<String> topics = new HashSet<>();
	private MessageProcessorChain chain;

	public ProcessorChainActor(MessageProcessorChain chain) {
		super();
		this.chain = chain;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchAny(msg -> {
			try {
				local.set(this);
				dispatch(msg);
			} finally {
				local.remove();
			}
		}).build();
	}

	public void dispatch(Object msg) {
		if (msg instanceof LocalPubSubMessage) {
			LocalPubSubMessage psm = (LocalPubSubMessage) msg;
			ActorRef mediator = DistributedPubSub.get(context().system()).mediator();
			String topic = psm.getTopic();
			if (Objects.equals(psm.getStatus(), Status.ACTIVE)) {
				mediator.tell(new DistributedPubSubMediator.Subscribe(topic, self()), self());
				topics.add(topic);
			} else {
				mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, self()), self());
				topics.remove(topic);
			}
			return;
		}
		try {
			chain.receive(context(), msg);
		} catch (Throwable e) {
			logger.error("process msg error msg:{} ex: {}", msg, e);
		}
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		ActorRef mediator = DistributedPubSub.get(context().system()).mediator();
		topics.stream().forEach(e -> mediator.tell(new DistributedPubSubMediator.Unsubscribe(e, self()), self()));
		topics.clear();
	}

	public static ProcessorChainActor getInstance() {
		return local.get();
	}
}