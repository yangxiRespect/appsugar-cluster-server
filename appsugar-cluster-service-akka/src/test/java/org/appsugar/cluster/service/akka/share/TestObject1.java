package org.appsugar.cluster.service.akka.share;

import java.io.Serializable;

public class TestObject1 implements Serializable {

	private int a = 111;
	private String name = "haha";
	private double aa;

	public TestObject1(String name) {
		super();
		this.name = name;
	}

	public TestObject1() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestObject1 [a=").append(a).append(", name=").append(name).append("]");
		return builder.toString();
	}

}
