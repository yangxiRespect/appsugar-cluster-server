package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 *  单个actor共享消息
 * @author NewYoung
 * 2016年5月27日下午4:33:43
 */
public class ActorClusterShareMessage implements Serializable {
	private static final long serialVersionUID = -7352838644480506887L;
	/**服务状态**/
	private ClusterStatus status;
	/**服务名称**/
	private String name;
	/**序列号后的引用**/
	private String ref;

	public ActorClusterShareMessage() {
		super();
	}

	public ActorClusterShareMessage(ClusterStatus status, String name, String ref) {
		super();
		this.status = status;
		this.name = name;
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public ClusterStatus getStatus() {
		return status;
	}

	public void setStatus(ClusterStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "ActorClusterShareMessage [status=" + status + ", name=" + name + ", ref=" + ref + "]";
	}

}