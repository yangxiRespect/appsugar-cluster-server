package org.appsugar.cluster.service.akka.system;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceRef;

/**
 * akka服务集群引用
 * @author NewYoung
 * 2016年5月30日下午2:25:33
 */
@SuppressWarnings("unused")
public class AkkaServiceClusterRef implements ServiceClusterRef {

	private List<AkkaServiceRef> serviceRefList = new CopyOnWriteArrayList<>();

	private List<ServiceRef> readonlyServiceRefList = Collections.unmodifiableList(serviceRefList);

	private volatile AkkaServiceRef min;

	private Random random = new Random();

	private int balanceSeed = 0;

	private String name;

	public AkkaServiceClusterRef(String name) {
		super();
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public int size() {
		return serviceRefList.size();
	}

	@Override
	public Iterable<ServiceRef> iterable() {
		return readonlyServiceRefList;
	}

	@Override
	public AkkaServiceRef random() {
		if (serviceRefList.isEmpty()) {
			return null;
		}
		int index = random.nextInt(serviceRefList.size());
		try {
			return serviceRefList.get(index);
		} catch (IndexOutOfBoundsException e) {
			return random();
		}
	}

	@Override
	public AkkaServiceRef balance() {
		if (serviceRefList.isEmpty()) {
			return null;
		}
		try {
			return serviceRefList.get(balanceSeed++ % serviceRefList.size());
		} catch (IndexOutOfBoundsException e) {
			return balance();
		}
	}

	@Override
	public ServiceRef balance(int seed) {
		if (serviceRefList.isEmpty()) {
			return null;
		}
		try {
			return serviceRefList.get(seed % serviceRefList.size());
		} catch (IndexOutOfBoundsException e) {
			return balance(seed);
		}
	}

	@Override
	public ServiceRef one() {
		if (serviceRefList.isEmpty()) {
			return null;
		}
		try {
			return serviceRefList.get(0);
		} catch (IndexOutOfBoundsException e) {
			return one();
		}
	}

	/**
	 * 添加服务引用
	 */
	void addServiceRef(AkkaServiceRef ref) {
		if (ref.hasLocalScope()) {
			serviceRefList.add(0, ref);
		} else {
			serviceRefList.add(ref);
		}
		if (Objects.isNull(min)) {
			min = ref;
		} else {
			min = ref.compareTo(min) < 0 ? ref : min;
		}
	}

	void removeServiceRef(AkkaServiceRef ref) {
		serviceRefList.remove(ref);
		if (serviceRefList.isEmpty()) {
			return;
		}
		if (ref == min) {
			min = Collections.min(serviceRefList);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AkkaServiceClusterRef [serviceRefList=").append(serviceRefList).append(", name=").append(name)
				.append("]");
		return builder.toString();
	}

	@Override
	public ServiceRef leader() {
		return min;
	}

}