package org.appsugar.cluster.service.akka.system;

import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;
import org.appsugar.cluster.service.api.ServiceRef;

public class RouterHelloService implements Service {

	private ServiceRef ref;

	public RouterHelloService(ServiceRef ref) {
		super();
		this.ref = ref;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		if (ref != null) {
			//请求服务后,执行context应该相同
			ref.ask(msg, e -> System.out.println(context == ServiceContextThreadLocal.context()),
					e -> System.out.println(context == ServiceContextThreadLocal.context()));
		}
		return 1;
	}

}
