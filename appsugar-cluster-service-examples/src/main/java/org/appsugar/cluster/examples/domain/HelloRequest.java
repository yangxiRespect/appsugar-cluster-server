package org.appsugar.cluster.examples.domain;

import java.io.Serializable;

public class HelloRequest implements Serializable {

	private static final long serialVersionUID = 3188077327131472088L;

	public String sender;
	public String message;

}
