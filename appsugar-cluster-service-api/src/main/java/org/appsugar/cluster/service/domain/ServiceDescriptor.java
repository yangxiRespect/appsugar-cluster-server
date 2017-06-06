package org.appsugar.cluster.service.domain;

import java.util.Arrays;
import java.util.List;

/**
 * 服务描述
 * @author NewYoung
 * 2016年12月8日下午2:55:12
 */
public class ServiceDescriptor {
	/**是否为本地服务(本地服务只能在同一jvm中调用)**/
	private boolean local;
	/**服务者**/
	private List<Object> serves;

	public ServiceDescriptor(Object... serves) {
		this(Arrays.asList(serves));
	}

	public ServiceDescriptor(List<Object> serves) {
		this(serves, false);
	}

	public ServiceDescriptor(List<Object> serves, boolean local) {
		super();
		this.serves = serves;
		this.local = local;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public List<Object> getServes() {
		return serves;
	}

	public void setServes(List<Object> serves) {
		this.serves = serves;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceDescriptor [local=").append(local).append(", serves=").append(serves).append("]");
		return builder.toString();
	}

}
