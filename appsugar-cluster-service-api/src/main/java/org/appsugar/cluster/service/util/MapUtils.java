package org.appsugar.cluster.service.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 提供些额外操作
 * @author NewYoung
 * 2017年5月25日下午1:27:24
 */
public class MapUtils {

	/**
	 * 根据key获取value,如果value不存在,那么调用supplier 获取值并存入map中
	 * @author NewYoung
	 * 2017年5月25日下午1:28:58
	 */
	public static final <K, V> V getOrCreate(Map<K, V> map, K key, Supplier<V> supplier) {
		V result = map.get(key);
		if (Objects.isNull(result)) {
			result = supplier.get();
			V oldValue = map.putIfAbsent(key, result);
			if (Objects.nonNull(oldValue)) {
				return oldValue;
			}
		}
		return result;
	}
}
