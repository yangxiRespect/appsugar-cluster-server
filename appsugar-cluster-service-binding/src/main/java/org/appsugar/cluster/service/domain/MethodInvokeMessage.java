package org.appsugar.cluster.service.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 方法调用消息
 * @author NewYoung
 * 2016年6月3日上午3:15:37
 */
public class MethodInvokeMessage implements Serializable {

	private static final long serialVersionUID = -7830127820021015454L;

	/**
	 * 接口全面+方法名+参数类型名称
	 */
	private List<String> nameList;

	/**
	 * 调用参数集合
	 */
	private Object[] params;

	public MethodInvokeMessage(List<String> nameList, Object[] params) {
		super();
		this.nameList = nameList;
		this.params = params;
	}

	public List<String> getNameList() {
		return nameList;
	}

	public Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvokeMessage [nameList=").append(nameList).append(", params=")
				.append(Arrays.toString(params)).append("]");
		return builder.toString();
	}

}