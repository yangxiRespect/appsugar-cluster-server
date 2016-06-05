package org.appsugar.cluster.service.binding;

import org.appsugar.cluster.service.api.Status;

/**
 * 服务状态改变监听
 * @author NewYoung
 * 2016年6月3日下午5:49:53
 */
public interface ServiceListener {

	void handle(String name, Status status);

}