package org.appsugar.cluster.service.akka.system;

import java.io.*;
import java.util.concurrent.*;
import akka.actor.*;

public class AskPatternEvent implements Serializable {

	private Object msg;
	private CompletableFuture future;
	private long timeout;
	private ActorRef destination;

}