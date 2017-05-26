package org.appsugar.cluster.service.domain;

/**
 * 集群节点
 * @author NewYoung
 * 2017年5月25日下午2:03:14
 */
public class ClusterMember {
	/**节点地址**/
	private String address;

	public ClusterMember() {
		super();
	}

	public ClusterMember(String address) {
		super();
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
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
		ClusterMember other = (ClusterMember) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClusterMember [address=").append(address).append("]");
		return builder.toString();
	}

}
