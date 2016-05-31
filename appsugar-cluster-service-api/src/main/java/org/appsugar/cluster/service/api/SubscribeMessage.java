package org.appsugar.cluster.service.api;

import java.io.Serializable;

/**
 * 关注与推送的消息
 * @author NewYoung
 * 2016年5月30日下午3:58:17
 */
public class SubscribeMessage implements Serializable {

	private static final long serialVersionUID = 7462487328738324864L;

	private String topic;

	private Object data;

	public SubscribeMessage(String topic, Object data) {
		super();
		this.topic = topic;
		this.data = data;
	}

	public SubscribeMessage() {
		super();
	}

	public String getTopic() {
		return topic;
	}

	public Object getData() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscribeEvent [topic=").append(topic).append(", data=").append(data).append("]");
		return builder.toString();
	}

}
