package org.appsugar.cluster.service.akka.share;

import java.util.List;

import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;

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

}