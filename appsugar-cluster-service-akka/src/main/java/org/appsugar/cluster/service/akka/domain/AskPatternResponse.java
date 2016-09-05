package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 * 响应
 * @author NewYoung
 * 2016年5月29日下午11:09:04
 */
public class AskPatternResponse implements Serializable {

	private static final long serialVersionUID = 1627536137594287371L;
	private int sequence;
	private Object data;

	public AskPatternResponse(int sequence, Object data) {
		super();
		this.sequence = sequence;
		this.data = data;
	}

	public AskPatternResponse() {
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
		builder.append("AskPatternResponse [sequence=").append(sequence).append(", data=").append(data).append("]");
		return builder.toString();
	}

}