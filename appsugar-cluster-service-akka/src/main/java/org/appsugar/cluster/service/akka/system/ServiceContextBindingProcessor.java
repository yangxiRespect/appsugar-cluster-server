package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.ServiceContextThreadLocal;

/**
 * 绑定servicecontext
 * 需要放在第一位
 * @author NewYoung
 * 2016年6月6日下午5:06:18
 */
public class ServiceContextBindingProcessor implements MessageProcessor {
	public static final String PROCESSOR_CONTEXT_KEY = "PROCESSOR_CONTEXT";
	private AkkaServiceContext ctx;
	private AkkaServiceClusterSystem system;

	public ServiceContextBindingProcessor(AkkaServiceClusterSystem system) {
		super();
		this.system = system;
	}

	@Override
	public Object process(ProcessorContext pctx, Object msg) throws Throwable {
		if (ctx == null) {
			ctx = new AkkaServiceContext(system.resolveRef(pctx.getSelf()), system);
		}
		try {
			ctx.addAttribute(PROCESSOR_CONTEXT_KEY, pctx);
			ctx.setSender(system.resolveRef(pctx.getSender()));
			//把当前上下文绑定到线程中, 等执行完后再清除
			ServiceContextThreadLocal.context(ctx);
			pctx.processNext(msg);
		} finally {
			ctx.setSender(null);
			ServiceContextThreadLocal.context(null);
		}
		return null;
	}

}
