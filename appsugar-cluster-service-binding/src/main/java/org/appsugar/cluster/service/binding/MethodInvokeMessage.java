package org.appsugar.cluster.service.binding;

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
	private int classNameHashCode;//类名hashCode
	private List<Integer> methodNameWithParamClassNameHashCodeList;//方法名和参数名hashCode集合
	private Object[] paramList;//调用参数

	public MethodInvokeMessage(int classNameHashCode, List<Integer> methodNameWithParamClassNameHashCodeList,
			Object[] paramList) {
		super();
		this.classNameHashCode = classNameHashCode;
		this.methodNameWithParamClassNameHashCodeList = methodNameWithParamClassNameHashCodeList;
		this.paramList = paramList;
	}

	public int getClassNameHashCode() {
		return classNameHashCode;
	}

	public void setClassNameHashCode(int classNameHashCode) {
		this.classNameHashCode = classNameHashCode;
	}

	public List<Integer> getMethodNameWithParamClassNameHashCodeList() {
		return methodNameWithParamClassNameHashCodeList;
	}

	public void setMethodNameWithParamClassNameHashCodeList(List<Integer> methodNameWithParamClassNameHashCodeList) {
		this.methodNameWithParamClassNameHashCodeList = methodNameWithParamClassNameHashCodeList;
	}

	public Object[] getParamList() {
		return paramList;
	}

	public void setParamList(Object[] paramList) {
		this.paramList = paramList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvokeMessage [classNameHashCode=").append(classNameHashCode)
				.append(", methodNameWithParamClassNameHashCodeList=").append(methodNameWithParamClassNameHashCodeList)
				.append(", paramList=").append(Arrays.toString(paramList)).append("]");
		return builder.toString();
	}

}