package org.appsugar.cluster.service.api;

import org.appsugar.cluster.service.domain.ClusterMember;
import org.appsugar.cluster.service.domain.Status;

/**
 * 节点状态监听
 * @author NewYoung
 * 2017年5月25日下午2:24:15
 */
public interface MemberStatusListener {
	/**
	 * member节点状态转变时被调用
	 * @author NewYoung
	 * 2017年5月25日下午2:25:36
	 */
	void handle(ClusterMember member, Status status);
}
