package org.appsugar.cluster.service.akka.share;

import java.io.Serializable;

import akka.actor.ActorRef;

/**
 * 分享的actor
 * @author NewYoung
 * 2016年5月27日下午4:11:06
 */
public class ActorShare implements Serializable {

	private static final long serialVersionUID = 3772912545747156967L;
	/**
	 * 自定义名称
	 */
	private String name;
	/**
	 * actor引用
	 * 禁止把actor引用已属性的方式在 cluster中传播
	 * 这样会引起第三方serialization异常.
	 */
	private transient ActorRef actorRef;

	public ActorShare(String name) {
		super();
		this.name = name;
		this.actorRef = actorRef;
	}

	public ActorShare() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ActorRef getActorRef() {
		return actorRef;
	}

	public void setActorRef(ActorRef actorRef) {
		this.actorRef = actorRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actorRef == null) ? 0 : actorRef.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActorShare other = (ActorShare) obj;
		if (actorRef == null) {
			if (other.actorRef != null)
				return false;
		} else if (!actorRef.equals(other.actorRef))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActorShare [name=").append(name).append(", actorRef=").append(actorRef).append("]");
		return builder.toString();
	}

}