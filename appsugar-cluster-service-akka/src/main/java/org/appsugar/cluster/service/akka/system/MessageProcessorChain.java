package org.appsugar.cluster.service.akka.system;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息处理链
 * @author NewYoung
 * 2016年5月29日下午5:56:24
 */
public class MessageProcessorChain {

	protected List<MessageProcessor> processorList;

	protected AkkaProcessorContext head;

	protected boolean needBuild;

	public MessageProcessorChain(List<MessageProcessor> processorList) {
		super();
		this.processorList = processorList;
		this.needBuild = true;
	}

	public MessageProcessorChain(MessageProcessor... processsors) {
		this(Arrays.asList(processsors));
	}

	public MessageProcessorChain() {
		this(new CopyOnWriteArrayList<>());
	}

	/**
	 * 处理消息
	 */
	public void receive(ActorContext ctx, Object msg) throws Throwable {
		if (needBuild) {
			needBuild = false;
			buildProcessorContext(ctx);
		}
		head.processNext(msg);
	}

	/**
	 * 添加一个处理器
	 */
	public void addLast(MessageProcessor p) {
		processorList.add(p);
		needBuild = true;
	}

	protected void buildProcessorContext(ActorContext ctx) {
		AkkaProcessorContext last = null;
		for (MessageProcessor processor : processorList) {
			if (last == null) {
				last = new AkkaProcessorContext(ctx, processor);
				head = new AkkaProcessorContext(ctx, null);
				head.setNext(last);
				continue;
			}
			AkkaProcessorContext current = new AkkaProcessorContext(ctx, processor);
			last.setNext(current);
			last = current;
		}
	}
}