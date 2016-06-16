package org.appsugar.cluster.service.binding;

import java.io.Serializable;

/**
 * 动态服务创建消息	
 * @author NewYoung
 * 2016年6月14日下午1:54:26
 */
public class DynamicServiceCreateMessage implements Serializable {

	private static final long serialVersionUID = -5809226359844579575L;
	private String sequence;

	public DynamicServiceCreateMessage(String sequence) {
		super();
		this.sequence = sequence;
	}

	public String getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DynamicServiceCreateMessage [sequence=").append(sequence).append("]");
		return builder.toString();
	}

}
