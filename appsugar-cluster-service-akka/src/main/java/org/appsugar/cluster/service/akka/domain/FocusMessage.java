package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 * 关注消息
 * @author NewYoung
 * 2017年5月11日下午3:41:35
 */
public class FocusMessage implements Serializable {
	private static final long serialVersionUID = -8551311715205494571L;
	/**
	 * 关注名称
	 */
	private String name;
	/**
	 * 是否前置匹配
	 */
	private boolean watchDynamic;

	public FocusMessage() {
		super();
	}

	public FocusMessage(String name) {
		this(name, false);
	}

	public FocusMessage(String name, boolean watchDynamic) {
		super();
		this.name = name;
		this.watchDynamic = watchDynamic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isWatchDynamic() {
		return watchDynamic;
	}

	public void setWatchDynamic(boolean watchDynamic) {
		this.watchDynamic = watchDynamic;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FocusMessage [name=").append(name).append(", watchDynamic=").append(watchDynamic).append("]");
		return builder.toString();
	}

}
