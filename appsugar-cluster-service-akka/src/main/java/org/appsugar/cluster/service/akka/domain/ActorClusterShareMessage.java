package org.appsugar.cluster.service.akka.domain;

import java.io.Serializable;

/**
 *  单个actor共享消息
 * @author NewYoung
 * 2016年5月27日下午4:33:43
 */
public class ActorClusterShareMessage implements Serializable {
	private static final long serialVersionUID = -7352838644480506887L;
	//状态
	private ClusterStatus status;
	//actor共享对象
	private ActorShare share;

	public ActorClusterShareMessage(ClusterStatus status, ActorShare share) {
		super();
		this.status = status;
		this.share = share;
	}

	public ActorClusterShareMessage() {
		super();
	}

	public ClusterStatus getStatus() {
		return status;
	}

	public void setStatus(ClusterStatus status) {
		this.status = status;
	}

	public ActorShare getShare() {
		return share;
	}

	public void setShare(ActorShare share) {
		this.share = share;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActorClusterShareMessage [status=").append(status).append(", share=").append(share).append("]");
		return builder.toString();
	}

}