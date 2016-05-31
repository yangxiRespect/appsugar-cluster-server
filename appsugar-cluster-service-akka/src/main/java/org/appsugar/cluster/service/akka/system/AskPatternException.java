package org.appsugar.cluster.service.akka.system;

import java.io.Serializable;

/**
 * 异常消息
 * @author NewYoung
 * 2016年5月30日上午11:36:40
 */
public class AskPatternException implements Serializable {

	private static final long serialVersionUID = -6261814043309917668L;

	private String msg;

	public AskPatternException(String msg) {
		super();
		this.msg = msg;
	}

	public AskPatternException() {
		super();
	}

	public String getMsg() {
		return msg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AskPatternException [msg=").append(msg).append("]");
		return builder.toString();
	}

}
