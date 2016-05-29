package org.appsugar.cluster.service.akka.system;

import java.util.Optional;
import java.util.function.Consumer;

import org.appsugar.cluster.service.api.ServiceRef;

import akka.actor.ActorRef;

public class AkkaServiceRef implements ServiceRef {

	private ActorRef destination;
	private String name;
	private ActorRef askPatternRef;

	/**
	 * 
	 * @param ref
	 * @param name
	 * @return 
	 */
	public AkkaServiceRef(ActorRef ref, String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T ask(Object msg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T ask(Object msg, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error, int timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tell(Object msg, ServiceRef sender) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasLocalScope() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> host() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Integer> hostPort() {
		// TODO Auto-generated method stub
		return null;
	}

}