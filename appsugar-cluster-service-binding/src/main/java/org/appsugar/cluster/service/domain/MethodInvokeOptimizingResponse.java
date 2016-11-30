package org.appsugar.cluster.service.domain;

import java.io.Serializable;

/**
 * 方法调用优化结果消息
 * @author NewYoung
 * 2016年6月12日上午10:04:50
 */
public class MethodInvokeOptimizingResponse implements Serializable {

	private static final long serialVersionUID = 2981299329008091576L;

	private int sequence;

	private Object result;

	public MethodInvokeOptimizingResponse(int sequence, Object result) {
		super();
		this.sequence = sequence;
		this.result = result;
	}

	public int getSequence() {
		return sequence;
	}

	public Object getResult() {
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvokeOptimizingResponse [sequence=").append(sequence).append(", result=").append(result)
				.append("]");
		return builder.toString();
	}

}
