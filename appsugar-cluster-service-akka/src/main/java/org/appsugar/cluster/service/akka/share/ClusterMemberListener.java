package org.appsugar.cluster.service.akka.share;

import org.appsugar.cluster.service.akka.domain.ClusterStatus;

import akka.cluster.Member;

/**
 * 集群节点监听器
 * @author NewYoung
 * 2016年5月27日下午4:27:26
 */
public interface ClusterMemberListener {

	/**
	 * 节点事件发生时回调方法
	 * @param m 节点成员
	 * @param state 状态
	 */
	void handle(Member m, ClusterStatus state);

}