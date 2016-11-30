package org.appsugar.cluster.service.domain;

import java.io.Serializable;

/**
 * 键值对
 * @author NewYoung
 * 2016年6月1日下午3:40:06
 * @param <K>
 * @param <V>
 */
public class KeyValue<K, V> implements Serializable {
	private static final long serialVersionUID = -7042898627294646413L;
	private K key;
	private V value;

	public KeyValue(K key, V value) {
		super();
		this.key = key;
		this.value = value;
	}

	public KeyValue() {
		super();
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KeyValue [key=").append(key).append(", value=").append(value).append("]");
		return builder.toString();
	}

}
