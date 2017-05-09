package org.appsugar.cluster.service.domain;

import org.appsugar.cluster.service.api.ServiceRef;

/**
 * 服务状态改变消息
 * @author NewYoung
 * 2016年6月3日上午3:37:59
 */
public class ServiceStatusMessage {
	private ServiceRef serviceRef;
	private Status status;

	public ServiceStatusMessage(ServiceRef serviceRef, Status status) {
		super();
		this.serviceRef = serviceRef;
		this.status = status;
	}

	public ServiceRef getServiceRef() {
		return serviceRef;
	}

	public void setServiceRef(ServiceRef serviceRef) {
		this.serviceRef = serviceRef;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceStatusMessage [serviceRef=").append(serviceRef.name()).append(", status=").append(status)
				.append("]");
		return builder.toString();
	}

}