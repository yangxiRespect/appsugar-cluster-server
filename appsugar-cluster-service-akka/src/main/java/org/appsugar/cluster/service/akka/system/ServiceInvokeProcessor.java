package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;

/**
 * 服务调用处理器,把所有消息交给service处理
 * 永远不会被并发,无需担心线程问题
 * @author NewYoung
 * 2016年5月29日下午2:30:59
 */
public class ServiceInvokeProcessor implements MessageProcessor {

	private Service service;

	public ServiceInvokeProcessor(Service service) {
		super();
		this.service = service;
	}

	@Override
	public Object process(ProcessorContext pctx, Object msg) throws Throwable {
		return service.handle(msg, ServiceContextThreadLocal.context());
	}

}