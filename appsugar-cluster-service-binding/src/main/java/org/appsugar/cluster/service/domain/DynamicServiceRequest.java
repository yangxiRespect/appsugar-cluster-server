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

	/**是否在本地创建**/
	private boolean location;

	public DynamicServiceRequest(String sequence) {
		this(sequence, false);
	}

	public DynamicServiceRequest(String sequence, Boolean location) {
		super();
		this.sequence = sequence;
		this.location = location;
	}

	public String getSequence() {
		return sequence;
	}

	public Boolean isLocation() {
		return location;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DynamicServiceRequest [sequence=").append(sequence).append("]");
		return builder.toString();
	}

}
