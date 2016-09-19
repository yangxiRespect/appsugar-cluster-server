package org.appsugar.cluster.service.binding.spring;

import org.appsugar.cluster.service.binding.ProxyServer;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * 代理对象spring实现
 * @author NewYoung
 * 2016年8月29日下午2:00:01
 */
public class SpringProxyServer implements ProxyServer {

	private Object target;

	public SpringProxyServer(Object target) {
		super();
		this.target = target;
	}

	@Override
	public Class<?> getTargetClass() {
		return AopUtils.getTargetClass(target);
	}

	@Override
	public Object getObject() {
		try {
			return AopUtils.isJdkDynamicProxy(target) ? ((Advised) target).getTargetSource().getTarget() : target;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
