package org.appsugar.cluster.service.domain;

import java.io.Serializable;
import java.util.Set;

/**
 * 集群节点状态消息
 * @author NewYoung
 * 2017年5月25日下午2:10:22
 */
public class ClusterMemberServiceMessage implements Serializable {
	private static final long serialVersionUID = -3228945895885152306L;
	/**节点所提供的服务**/
	private Set<String> supplyServices;
	/**关注的服务**/
	private Set<String> focusServices;
	/**特殊关注服务**/
	private Set<String> specialFocusServices;

	public ClusterMemberServiceMessage() {
		super();
	}

	public ClusterMemberServiceMessage(Set<String> supplyServices, Set<String> focusServices,
			Set<String> specialFocusServices) {
		super();
		this.supplyServices = supplyServices;
		this.focusServices = focusServices;
		this.specialFocusServices = specialFocusServices;
	}

	public Set<String> getSupplyServices() {
		return supplyServices;
	}

	public void setSupplyServices(Set<String> supplyServices) {
		this.supplyServices = supplyServices;
	}

	public Set<String> getFocusServices() {
		return focusServices;
	}

	public void setFocusServices(Set<String> focusServices) {
		this.focusServices = focusServices;
	}

	public Set<String> getSpecialFocusServices() {
		return specialFocusServices;
	}

	public void setSpecialFocusServices(Set<String> specialFocusServices) {
		this.specialFocusServices = specialFocusServices;
	}

	@Override
	public String toString() {
		return "ClusterMemberStatusMessage [supplyServices=" + supplyServices + ", focusServices=" + focusServices
				+ ", specialFocusServices=" + specialFocusServices + "]";
	}

}
