package org.appsugar.cluster.service.akka.system;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.appsugar.cluster.service.api.ServiceContext;

/**
 * akka服务上下文
 * @author NewYoung
 * 2016年5月29日下午2:40:59
 */
public class AkkaServiceContext implements ServiceContext {

	private AkkaServiceRef self;
	private AkkaServiceRef sender;
	private AkkaServiceClusterSystem system;
	private Map<Object, Object> attributes = new HashMap<>();

	public AkkaServiceContext(AkkaServiceRef self, AkkaServiceClusterSystem system) {
		super();
		this.self = self;
		this.system = system;
	}

	@Override
	public AkkaServiceRef self() {
		return self;
	}

	@Override
	public AkkaServiceRef sender() {
		return sender;
	}

	@Override
	public AkkaServiceClusterSystem system() {
		return system;
	}

	@Override
	public void addAttribute(Object name, Object value) {
		attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(Object name) {
		return (T) attributes.get(name);
	}

	@Override
	public Map<Object, Object> attributes() {
		return Collections.unmodifiableMap(attributes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeAttribute(Object name) {
		return (T) attributes.remove(name);
	}

	protected void setSender(AkkaServiceRef sender) {
		this.sender = sender;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AkkaServiceContext [self=").append(self).append(", sender=").append(sender)
				.append(", attributes=").append(attributes).append("]");
		return builder.toString();
	}

}