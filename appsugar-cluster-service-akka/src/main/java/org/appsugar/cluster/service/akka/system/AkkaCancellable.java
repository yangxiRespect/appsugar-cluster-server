package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.Cancellable;

/**
 * 可关闭接口
 * @author NewYoung
 * 2016年5月30日下午5:11:01
 */
public class AkkaCancellable implements Cancellable {

	private akka.actor.Cancellable cancellable;

	public AkkaCancellable(akka.actor.Cancellable cancellable) {
		super();
		this.cancellable = cancellable;
	}

	@Override
	public boolean cancel() {
		return cancellable.cancel();
	}

	@Override
	public boolean isCancelled() {
		return cancellable.isCancelled();
	}

}
