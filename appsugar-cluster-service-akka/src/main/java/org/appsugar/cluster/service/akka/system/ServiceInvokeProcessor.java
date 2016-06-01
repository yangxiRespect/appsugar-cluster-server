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
	private AkkaServiceContext ctx;
	private AkkaServiceClusterSystem system;

	public ServiceInvokeProcessor(Service service, AkkaServiceClusterSystem system) {
		super();
		this.service = service;
		this.system = system;
	}

	@Override
	public Object process(ProcessorContext pctx, Object msg) throws Exception {
		if (ctx == null) {
			ctx = new AkkaServiceContext(system.resolveRef(pctx.getSelf()), system);
		}
		try {
			ctx.setSender(system.resolveRef(pctx.getSender()));
			//把当前上下文绑定到线程中, 等执行完后再清除
			ServiceContextThreadLocal.context(ctx);
			return service.handle(msg, ctx);
		} finally {
			ctx.setSender(null);
			ServiceContextThreadLocal.context(null);
		}
	}

}