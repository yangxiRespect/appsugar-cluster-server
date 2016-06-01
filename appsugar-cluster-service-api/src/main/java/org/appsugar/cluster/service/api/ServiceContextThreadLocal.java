package org.appsugar.cluster.service.api;

/**
 * 服务上下文线程存储
 * @author NewYoung
 * 2016年5月29日下午2:32:49
 */
public class ServiceContextThreadLocal {

	private static ThreadLocal<ServiceContext> local = new ThreadLocal<>();

	/**
	 * 获取服务 
	 */
	public static ServiceContext context() {
		return local.get();
	}

	/**
	 * 设置服务
	 */
	public static void context(ServiceContext ctx) {
		if (ctx == null) {
			local.remove();
			return;
		}
		local.set(ctx);
	}

}