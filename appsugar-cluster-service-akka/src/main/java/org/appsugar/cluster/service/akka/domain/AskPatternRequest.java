package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 * 请求
 * @author NewYoung
 * 2016年5月29日下午11:09:09
 */
public class AskPatternRequest implements Serializable {

	private static final long serialVersionUID = 5681710765088579747L;
	private int sequence;
	private Object data;

	public AskPatternRequest(int sequence, Object data) {
		super();
		this.sequence = sequence;
		this.data = data;
	}

	public AskPatternRequest() {
		super();
	}

	public int getSequence() {
		return sequence;
	}

	public Object getData() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AskPatternRequest [sequence=").append(sequence).append(", data=").append(data).append("]");
		return builder.toString();
	}

}