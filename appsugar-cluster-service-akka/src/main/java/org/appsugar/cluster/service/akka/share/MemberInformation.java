package org.appsugar.cluster.service.akka.share;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.appsugar.cluster.service.akka.domain.ActorShare;

/**
 * 节点信息
 * @author NewYoung
 * 2017年5月11日下午3:54:15
 */
public class MemberInformation {
	/**节点所有共享服务列表**/
	private List<ActorShare> shareActorList = new LinkedList<>();
	/**服务数映射**/
	private Map<String, Integer> supplyMap = new ConcurrentHashMap<>();
	/**节点所关心的服务**/
	private Set<String> focusSet = ConcurrentHashMap.newKeySet();
	/**节点所关心特殊服务**/
	private Set<String> focusSpecialSet = ConcurrentHashMap.newKeySet();

	/**
	 * 是否关注该服务
	 * @author NewYoung
	 * 2017年5月11日下午4:14:03
	 */
	public boolean focusOn(String name) {
		return focusSet.contains(name);
	}

	/**
	 * 是否提供该服务
	 * @author NewYoung
	 * 2017年6月15日下午3:43:13
	 */
	public boolean supplyOn(String name) {
		return supplyMap.getOrDefault(name, 0) > 0;
	}

	/**
	 * 是否关注特殊服务
	 * @author NewYoung
	 * 2017年5月11日下午5:49:59
	 */
	public boolean focusOnSpecial(String name) {
		return focusSpecialSet.contains(name);
	}

	public List<ActorShare> getShareActorList() {
		return shareActorList;
	}

	public Set<String> getFocusSet() {
		return focusSet;
	}

	public Set<String> getFocusSpecialSet() {
		return focusSpecialSet;
	}

	public void addActorShare(ActorShare as) {
		shareActorList.add(as);
		adjust(as.getName(), 1);
	}

	public void removeActorShare(ActorShare as) {
		shareActorList.remove(as);
		adjust(as.getName(), -1);
	}

	void adjust(String name, int value) {
		int oldValue = supplyMap.getOrDefault(name, 0);
		supplyMap.put(name, oldValue + value);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MemberInformation [shareActorList=").append(shareActorList).append(", focusSet=")
				.append(focusSet).append(", focusSpecialSet=").append(focusSpecialSet).append("]");
		return builder.toString();
	}

}
