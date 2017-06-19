package org.appsugar.cluster.service.api;

import java.util.List;

import org.appsugar.cluster.service.domain.Status;

/**
 * 服务状态监听
 * @author NewYoung
 * 2016年6月1日下午1:39:22
 */
public interface ServiceStatusListener {

	public void handle(List<ServiceRef> serviceRef, Status status);

}
