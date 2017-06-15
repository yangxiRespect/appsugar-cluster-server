package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	private Set<String> names;
	/**
	 * 是否前置匹配
	 */
	private boolean watchDynamic;

	public FocusMessage() {
		super();
	}

	public FocusMessage(String name) {
		this(new HashSet<>(Arrays.asList(name)));
	}

	public FocusMessage(String name, boolean watchDynamic) {
		this(new HashSet<>(Arrays.asList(name)), watchDynamic);
	}

	public FocusMessage(Set<String> names) {
		this(names, false);
	}

	public FocusMessage(Set<String> names, boolean watchDynamic) {
		super();
		this.names = names;
		this.watchDynamic = watchDynamic;
	}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}

	public boolean isWatchDynamic() {
		return watchDynamic;
	}

	public void setWatchDynamic(boolean watchDynamic) {
		this.watchDynamic = watchDynamic;
	}

	@Override
	public String toString() {
		return "FocusMessage [names=" + names + ", watchDynamic=" + watchDynamic + "]";
	}

}
