package org.appsugar.cluster.service.akka.serialization;

import java.io.Serializable;

/**
 * protostuff统一包装对象
 * @author NewYoung
 * 2016年5月28日下午11:39:09
 */
public class ProtostuffObjectWrapper implements Serializable {

	private static final long serialVersionUID = 3695585385835006094L;
	private Object object;

	public ProtostuffObjectWrapper(Object object) {
		super();
		this.object = object;
	}

	public ProtostuffObjectWrapper() {
		super();
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProtostuffWrapper [object=").append(object).append("]");
		return builder.toString();
	}

}
