package org.appsugar.cluster.examples.facade;

import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.annotation.DynamicService;

/**
 * 动态网关服务
 * @author shenliuyang@gmail.com
 */
@DynamicService(GatewayFacade.name)
public interface GatewayFacade {
	public static final String name = "gateway";

	/**
	 * 向指定通道id发送消息
	 */
	public CompletableFuture<Void> tell(String channelId, String msg);
}
