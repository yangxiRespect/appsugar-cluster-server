package org.appsugar.cluster.service.akka.system;

import java.util.Map;

public class AskPatternMessageProcessor implements MessageProcessor {

	private Map<Integer, RequestMarker> markers;

	@Override
	public Object process(ProcessorContext ctx, Object msg) {
		return null;
	}

}