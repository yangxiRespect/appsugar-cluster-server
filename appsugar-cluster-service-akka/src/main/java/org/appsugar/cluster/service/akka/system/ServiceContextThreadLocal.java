package org.appsugar.cluster.service.akka.system;

/**
 * 服务上下文线程存储
 * @author NewYoung
 * 2016年5月29日下午2:32:49
 */
public class ServiceContextThreadLocal {

	private static ThreadLocal<AkkaServiceContext> local = new ThreadLocal<>();

	/**
	 * 获取服务 
	 */
	public static AkkaServiceContext context() {
		return local.get();
	}

	/**
	 * 设置服务
	 */
	protected static void context(AkkaServiceContext ctx) {
		if (ctx == null) {
			local.remove();
			return;
		}
		local.set(ctx);
	}

}