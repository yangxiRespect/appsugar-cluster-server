package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 * 重复消息,用来处理请求超时
 * @author NewYoung
 * 2016年5月29日下午4:25:40
 */
public class RepeatEvent implements Serializable {
	private static final long serialVersionUID = -7838378721201142389L;
	public static final RepeatEvent INSTANCE = new RepeatEvent();
}