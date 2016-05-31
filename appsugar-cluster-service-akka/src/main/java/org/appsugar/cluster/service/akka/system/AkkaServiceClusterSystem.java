package org.appsugar.cluster.service.akka.system;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.appsugar.cluster.service.akka.share.ActorShareSystem;
import org.appsugar.cluster.service.akka.share.ClusterStatus;
import org.appsugar.cluster.service.api.Cancellable;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.api.SubscribeMessage;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import scala.concurrent.duration.Duration;

/**
 * akka服务集群系统
 * @author NewYoung
 * 2016年5月30日下午2:58:10
 */
public class AkkaServiceClusterSystem implements ServiceClusterSystem {

	private ActorSystem system;
	private ActorShareSystem actorShareSystem;
	private Map<Service, Object> localServices = new ConcurrentHashMap<>();
	private Map<String, AkkaServiceClusterRef> serviceClusterRefs = new ConcurrentHashMap<>();
	private Map<ActorRef, AkkaServiceRef> actorRefMapping = new ConcurrentHashMap<>();

	//本地actor名称,从0开始.
	private AtomicInteger actorNameGenerator = new AtomicInteger(0);

	/**
	 * 服务系统构造
	 * @param name 系统名称
	 * @param config 配置信息
	 */
	public AkkaServiceClusterSystem(String name, Config config) {
		system = ActorSystem.create(name, config);
		actorShareSystem = ActorShareSystem.getSystem(system, (a, s) -> {
			if (ClusterStatus.UP == s) {
				a.stream().forEach(ref -> {
					String shareName = ref.getName();
					ActorRef actorRef = ref.getActorRef();
					MessageProcessorChain chain = new MessageProcessorChain(new AskPatternMessageProcessor());
					ActorRef askPatternRef = system.actorOf(Props.create(ProcessorChainActor.class, chain),
							"" + actorNameGenerator.getAndIncrement());
					AkkaServiceRef akkaServiceRef = new AkkaServiceRef(actorRef, shareName, askPatternRef);
					actorRefMapping.put(actorRef, akkaServiceRef);
					getAndCreateServiceClusterRef(shareName).addServiceRef(akkaServiceRef);
				});
			} else {
				a.stream().forEach(ref -> {
					if (!actorRefMapping.containsKey(ref.getActorRef())) {
						return;
					}
					AkkaServiceClusterRef clusterRef = serviceClusterRefs.get(ref.getName());
					AkkaServiceRef serviceRef = actorRefMapping.remove(ref.getActorRef());
					clusterRef.removeServiceRef(serviceRef);
					system.stop(serviceRef.askPatternActorRef());
				});
			}
		});
	}

	/**
	 * 根据actorRef获取对应服务引用
	 */
	public AkkaServiceRef resolveRef(ActorRef ref) {
		AkkaServiceRef akkaServiceRef = actorRefMapping.get(ref);
		return akkaServiceRef != null ? akkaServiceRef : new ResponseOnlyServiceRef(ref, "ResponseOnlyServiceRef");
	}

	@Override
	public void subscribe(String topic, ServiceRef ref) {
		if (!ref.hasLocalScope()) {
			throw new RuntimeException("Subscriber do not allowed remote ServiceRef");
		}
		AkkaServiceRef akkaServiceRef = (AkkaServiceRef) ref;
		ActorRef actorRef = akkaServiceRef.destination();
		ActorRef mediator = DistributedPubSub.get(system).mediator();
		//关注
		mediator.tell(new DistributedPubSubMediator.Subscribe(topic, actorRef), actorRef);
		//向关注列表推送 我的关注事件
		publish(topic, new SubscribeMessage(topic, ClusterStatus.UP), ref);
	}

	@Override
	public void publish(String topic, Object msg, ServiceRef sender) {
		AkkaServiceRef akkaServiceRef = (AkkaServiceRef) sender;
		ActorRef actorRef = akkaServiceRef.destination();
		ActorRef mediator = DistributedPubSub.get(system).mediator();
		mediator.tell(new DistributedPubSubMediator.Publish(topic, new SubscribeMessage(topic, msg)), actorRef);
	}

	@Override
	public ServiceRef serviceFor(Service service, String name) {
		//臣妾只能做到这了
		synchronized (name.intern()) {
			if (localServices.containsKey(service)) {
				throw new RuntimeException("service already register " + "name " + name);
			}
			localServices.put(service, name);
			MessageProcessorChain chain = new MessageProcessorChain(new AskPatternMessageProcessor(),
					new ServiceInvokeProcessor(service, this));
			//创建一个service actor TODO 减少actor名称长度, 
			ActorRef ref = system.actorOf(Props.create(ProcessorChainActor.class, chain),
					actorNameGenerator.getAndIncrement() + "");
			try {
				actorShareSystem.share(ref, name).get();
				return actorRefMapping.get(ref);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public ServiceClusterRef serviceOf(String name) {
		return serviceClusterRefs.get(name);
	}

	@Override
	public Iterable<ServiceClusterRef> services() {
		return new ArrayList<>(serviceClusterRefs.values());
	}

	@Override
	public void terminate() {
		system.terminate();
	}

	@Override
	public Cancellable schedule(ServiceRef serviceRef, long time, Object msg) {
		AkkaServiceRef ref = (AkkaServiceRef) serviceRef;
		Scheduler scheduler = system.scheduler();
		akka.actor.Cancellable c = scheduler.schedule(Duration.create(time, TimeUnit.MILLISECONDS),
				Duration.create(time, TimeUnit.MILLISECONDS), ref.destination(), msg, system.dispatcher(), null);
		return new AkkaCancellable(c);
	}

	private AkkaServiceClusterRef getAndCreateServiceClusterRef(String name) {
		AkkaServiceClusterRef ref = serviceClusterRefs.get(name);
		if (ref == null) {
			ref = new AkkaServiceClusterRef(name);
			serviceClusterRefs.put(name, ref);
		}
		return ref;
	}
}