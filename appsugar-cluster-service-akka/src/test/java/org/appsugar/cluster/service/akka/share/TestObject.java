package org.appsugar.cluster.service.akka.share;

import java.io.Serializable;

public class TestObject implements Serializable {

	private String name = "haha";

	public TestObject(String name) {
		super();
		this.name = name;
	}

	public TestObject() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestObject [name=").append(name).append("]");
		return builder.toString();
	}

}
