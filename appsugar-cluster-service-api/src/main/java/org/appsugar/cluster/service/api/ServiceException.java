package org.appsugar.cluster.service.api;

/**
 * 服务异常
 * @author NewYoung
 * 2016年5月30日下午1:11:16
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = -3249751256559601223L;

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

}
