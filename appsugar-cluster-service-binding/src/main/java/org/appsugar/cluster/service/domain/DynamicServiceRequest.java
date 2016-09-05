package org.appsugar.cluster.service.domain;

import java.io.Serializable;

/**
 * 动态服务创建请求
 * @author NewYoung
 * 2016年6月14日下午1:53:18
 */
public class DynamicServiceRequest implements Serializable {

	private static final long serialVersionUID = -1883477806335066396L;

	private String sequence;

	public DynamicServiceRequest(String sequence) {
		super();
		this.sequence = sequence;
	}

	public String getSequence() {
		return sequence;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DynamicServiceRequest [sequence=").append(sequence).append("]");
		return builder.toString();
	}

}
