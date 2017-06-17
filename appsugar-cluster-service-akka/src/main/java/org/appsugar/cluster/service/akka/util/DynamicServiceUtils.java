package org.appsugar.cluster.service.akka.util;

public class DynamicServiceUtils {
	/**
	 * 根据sequence获取动态服务名称
	 */
	public static final String getDynamicServiceNameWithSequence(String name, String sequence) {
		return name + "/" + sequence;
	}

	/**
	 * 获取动态服务sequence
	 * @author NewYoung
	 * 2017年3月23日下午4:41:31
	 */
	public static final String getDynamicServiceSequenceByName(String name, String serviceName) {
		if (!serviceName.startsWith(name)) {
			return null;
		}
		int theLastSeparateIndex = serviceName.lastIndexOf(name + "/");
		if (theLastSeparateIndex == -1) {
			return null;
		}
		return serviceName.substring(theLastSeparateIndex + 1 + name.length());
	}

	/**
	 * 判断是否是当前服务的子服务
	 * @author NewYoung
	 * 2017年5月11日下午4:50:51
	 */
	public static final boolean isDynamicServiceAs(String name, String serviceName) {
		if (!serviceName.startsWith(name)) {
			return false;
		}
		int theLastSeparateIndex = serviceName.lastIndexOf(name + "/");
		if (theLastSeparateIndex == -1) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否动态服务
	 * @author NewYoung
	 * 2017年5月11日下午5:41:56
	 */
	public static final boolean isDynamiceService(String serviceName) {
		return serviceName.indexOf('/') != -1;
	}

	/**
	 * 获取动态服务创建名
	 * @author NewYoung
	 * 2017年5月11日下午5:45:44
	 */
	public static final String getDynamicServiceFirstName(String serviceName) {
		int index = serviceName.indexOf('/');
		if (index == -1) {
			return null;
		}
		return serviceName.substring(0, index);
	}
}
