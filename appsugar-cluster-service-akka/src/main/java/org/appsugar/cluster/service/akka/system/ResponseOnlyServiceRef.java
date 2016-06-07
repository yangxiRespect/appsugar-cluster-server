package org.appsugar.cluster.service.akka.system;

import java.util.function.Consumer;

import akka.actor.ActorRef;

/**
 * 响应服务引用.
 * 不支持ask
 * @author NewYoung
 * 2016年5月29日下午4:26:16
 */
public class ResponseOnlyServiceRef extends AkkaServiceRef {

	private static final String ERROR = " response only service ref ,do not support ask pattern";

	public ResponseOnlyServiceRef(ActorRef destination, String name) {
		super(destination, name);
	}

	@Override
	public <T> T ask(Object msg) {
		throw new UnsupportedOperationException(ERROR);
	}

	@Override
	public <T> T ask(Object msg, long timeout) {
		throw new UnsupportedOperationException(ERROR);
	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error) {
		throw new UnsupportedOperationException(ERROR);
	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error, long timeout) {
		throw new UnsupportedOperationException(ERROR);
	}

}