package org.appsugar.cluster.service.domain;

import org.appsugar.cluster.service.domain.Status;

/**
 * 服务状态改变消息
 * @author NewYoung
 * 2016年6月3日上午3:37:59
 */
public class ServiceStatusMessage {
	private String name;
	private Status status;

	public ServiceStatusMessage(String name, Status status) {
		super();
		this.name = name;
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		builder.append("ServiceStatusMessage [name=").append(name).append(", status=").append(status).append("]");
		return builder.toString();
	}

}