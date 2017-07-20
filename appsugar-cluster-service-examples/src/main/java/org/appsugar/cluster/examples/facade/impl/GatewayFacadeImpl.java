package org.appsugar.cluster.examples.facade.impl;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.examples.facade.GatewayFacade;

/**
 * 网关服务实现
 * @author shenliuyang@gmail.com
 *
 */
public class GatewayFacadeImpl implements GatewayFacade {

	private String id;

	public GatewayFacadeImpl(String id) {
		super();
		this.id = id;
	}

	@Override
	public CompletableFuture<Void> tell(String channelId, String msg) {
		System.out.println("channel id is " + channelId + " msg is " + msg);
		return CompletableFuture.completedFuture(null);
	}

}
