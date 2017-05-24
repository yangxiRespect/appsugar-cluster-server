package org.appsugar.cluster.service.domain;

/**
 * 节点
 * @author NewYoung
 * 2017年5月24日下午1:36:04
 */
public class Member {
	private String host;

	public Member() {
		super();
	}

	public Member(String host) {
		super();
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Member [host=").append(host).append("]");
		return builder.toString();
	}

}
