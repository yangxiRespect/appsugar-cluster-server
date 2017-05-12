package org.appsugar.cluster.service.akka.share;

import java.util.List;

import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;
import org.appsugar.cluster.service.akka.domain.FocusMessage;

import akka.actor.ActorRef;

/**
 * actor共享消息监听器
 * @author NewYoung
 * 2016年5月27日下午4:30:16
 */
public interface ActorShareListener {

	/**
	 * 处理actor共享消息
	 * @param actors 一堆actor共享
	 * @param status 状态
	 */
	void handle(List<ActorShare> actors, ClusterStatus status);

	/**
	 * 节点关注消息处理
	 * @author NewYoung
	 * 2017年5月11日下午4:31:37
	 */
	default void memberFocus(ActorRef actor, FocusMessage msg) {
		//do nothing
	}

}