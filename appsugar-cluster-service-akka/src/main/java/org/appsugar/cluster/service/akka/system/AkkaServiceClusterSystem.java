package org.appsugar.cluster.service.akka.system;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.akka.share.ActorShare;
import org.appsugar.cluster.service.akka.share.ActorShareListener;
import org.appsugar.cluster.service.akka.share.ActorShareSystem;
import org.appsugar.cluster.service.akka.share.ClusterStatus;
import org.appsugar.cluster.service.api.Cancelable;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class AkkaServiceClusterSystem implements ServiceClusterSystem, ActorShareListener {

	private ActorSystem system;
	private Map<Service, Object> localServices;
	private ActorShareSystem actorShareSystem;
	private Map<String, AkkaServiceClusterRef> serviceClusterRefs;
	private Map<ActorRef, AkkaServiceRef> actorRefMapping;

	/**
	 * 
	 * @param name
	 * @param config
	 * @return 
	 */
	public void ServiceClusterAkkaSystem(String name, Config config) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param ref
	 * @return 
	 */
	public AkkaServiceRef resolveRef(ActorRef ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void subscribe(ServiceRef ref, String topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publish(String topic, Object msg, ServiceRef sender) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handle(List<ActorShare> actors, ClusterStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public ServiceRef serviceFor(Service service, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceClusterRef serviceOf(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ServiceClusterRef> services() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> terminate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cancelable schedule(ServiceRef serviceRef, long time, Object msg) {
		// TODO Auto-generated method stub
		return null;
	}

}