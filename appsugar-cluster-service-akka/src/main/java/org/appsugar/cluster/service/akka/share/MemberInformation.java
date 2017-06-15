package org.appsugar.cluster.service.akka.share;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.appsugar.cluster.service.akka.domain.ActorShare;

/**
 * 节点信息
 * @author NewYoung
 * 2017年5月11日下午3:54:15
 */
public class MemberInformation {
	/**节点所有共享服务列表**/
	private List<ActorShare> shareActorList = new ArrayList<>();
	/**节点所关心的服务**/
	private Set<String> focusSet = new HashSet<>();
	/**节点所关心特殊服务**/
	private Set<String> focusSpecialSet = new HashSet<>();

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
		return shareActorList.stream().anyMatch(e -> Objects.equals(e.getName(), name));
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MemberInformation [shareActorList=").append(shareActorList).append(", focusSet=")
				.append(focusSet).append(", focusSpecialSet=").append(focusSpecialSet).append("]");
		return builder.toString();
	}

}
