package org.appsugar.cluster.service.domain;

import java.io.Serializable;

/**
 * 集群资源信息消息
 * @author NewYoung
 * 2017年5月27日上午10:07:51
 */
public class ClusterMemberResourceMessage implements Serializable {

	private static final long serialVersionUID = -7760330463170403424L;

	private int availableProcessors;
	private long vmFreeMemroy;
	private long vmTotalMemory;
	private int vmThreadCount;
	private int vmDaemonThreadCount;

	public ClusterMemberResourceMessage() {
		super();
	}

	public ClusterMemberResourceMessage(int availableProcessors, long vmFreeMemroy, long vmTotalMemory,
			int vmThreadCount, int vmDaemonThreadCount) {
		super();
		this.availableProcessors = availableProcessors;
		this.vmFreeMemroy = vmFreeMemroy;
		this.vmTotalMemory = vmTotalMemory;
		this.vmThreadCount = vmThreadCount;
		this.vmDaemonThreadCount = vmDaemonThreadCount;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public long getVmFreeMemroy() {
		return vmFreeMemroy;
	}

	public void setVmFreeMemroy(long vmFreeMemroy) {
		this.vmFreeMemroy = vmFreeMemroy;
	}

	public long getVmTotalMemory() {
		return vmTotalMemory;
	}

	public void setVmTotalMemory(long vmTotalMemory) {
		this.vmTotalMemory = vmTotalMemory;
	}

	public int getVmThreadCount() {
		return vmThreadCount;
	}

	public void setVmThreadCount(int vmThreadCount) {
		this.vmThreadCount = vmThreadCount;
	}

	public int getVmDaemonThreadCount() {
		return vmDaemonThreadCount;
	}

	public void setVmDaemonThreadCount(int vmDaemonThreadCount) {
		this.vmDaemonThreadCount = vmDaemonThreadCount;
	}

	@Override
	public String toString() {
		return "ClusterMemberResourceMessage [availableProcessors=" + availableProcessors + ", vmFreeMemroy="
				+ vmFreeMemroy + ", vmTotalMemory=" + vmTotalMemory + ", vmThreadCount=" + vmThreadCount
				+ ", vmDaemonThreadCount=" + vmDaemonThreadCount + "]";
	}

}
