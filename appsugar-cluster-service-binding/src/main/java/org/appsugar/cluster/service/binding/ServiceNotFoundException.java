package org.appsugar.cluster.service.binding;

/**
 * 服务未找到异常
 * @author NewYoung
 * 2016年6月16日上午10:27:08
 */
public class ServiceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8951584971195120007L;

	public ServiceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceNotFoundException(String message) {
		super(message);
	}

}
