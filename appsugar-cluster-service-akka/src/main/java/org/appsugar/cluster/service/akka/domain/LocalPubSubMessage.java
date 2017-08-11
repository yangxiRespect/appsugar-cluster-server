package org.appsugar.cluster.service.akka.domain;

import org.appsugar.cluster.service.domain.Status;

/**
 * 分布式发送接收消息
 * @author NewYoung
 *
 */
public class LocalPubSubMessage {

	private String topic;

	private Status status;

	public LocalPubSubMessage(String topic, Status status) {
		super();
		this.topic = topic;
		this.status = status;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "LocalPubSubMessage [topic=" + topic + ", status=" + status + "]";
	}

}
