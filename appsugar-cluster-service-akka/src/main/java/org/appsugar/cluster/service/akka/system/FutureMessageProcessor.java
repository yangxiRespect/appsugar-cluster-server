package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.FutureMessage;

/**
 * 异步消息执行处理器
 * @author NewYoung
 * 2016年6月6日下午4:08:58
 */
public class FutureMessageProcessor implements MessageProcessor {

	@Override
	public Object process(ProcessorContext ctx, Object msg) throws Throwable {
		if (msg instanceof FutureMessage) {
			@SuppressWarnings("unchecked")
			FutureMessage<Object> futureMessage = (FutureMessage<Object>) msg;
			futureMessage.getConsumer().accept(futureMessage.getResult(), futureMessage.getThrowable());
			return null;
		}
		return ctx.processNext(msg);
	}

}
