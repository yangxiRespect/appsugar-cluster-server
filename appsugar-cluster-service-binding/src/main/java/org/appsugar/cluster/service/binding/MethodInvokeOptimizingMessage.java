package org.appsugar.cluster.service.binding;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 方法调用消息
 * @author NewYoung
 * 2016年6月3日上午3:15:37
 */
public class MethodInvokeOptimizingMessage implements Serializable {

	private static final long serialVersionUID = -7830127820021015454L;

	/**
	 * 方法对应序列
	 */
	private int sequence;

	/**
	 * 调用参数集合
	 */
	private Object[] params;

	public MethodInvokeOptimizingMessage(int sequence, Object[] params) {
		super();
		this.sequence = sequence;
		this.params = params;
	}

	public int getSequence() {
		return sequence;
	}

	public Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvokeOptimizingMessage [sequence=").append(sequence).append(", params=")
				.append(Arrays.toString(params)).append("]");
		return builder.toString();
	}

}