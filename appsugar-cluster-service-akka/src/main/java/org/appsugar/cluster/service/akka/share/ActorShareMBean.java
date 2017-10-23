package org.appsugar.cluster.service.akka.share;

import java.util.Set;

/**
 * 监控
 * @author NewYoung
 * 2017年7月17日下午4:34:33
 */
public interface ActorShareMBean {
	/**
	 * 我提供的服务
	 * @author NewYoung
	 * 2017年7月17日下午4:36:39
	 */
	Set<String> provideServices();

	/**
	 * 其他节点提供的服务
	 * @author NewYoung
	 * 2017年7月17日下午4:37:09
	 */
	Set<String> memberShareService();
	
	/**
	 * 重新发布
	 */
	String   reShare();
}
