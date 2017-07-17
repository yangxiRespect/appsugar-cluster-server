package org.appsugar.cluster.service.akka.system;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;
import org.appsugar.cluster.service.akka.share.ActorShareSystem;
import org.appsugar.cluster.service.api.Cancellable;
import org.appsugar.cluster.service.api.MemberStatusListener;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.api.ServiceStatusListener;
import org.appsugar.cluster.service.domain.ClusterMember;
import org.appsugar.cluster.service.domain.ClusterMemberResourceMessage;
import org.appsugar.cluster.service.domain.ClusterMemberServiceMessage;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.domain.SubscribeMessage;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import scala.Option;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * akka服务集群系统
 * @author NewYoung
 * 2016年5月30日下午2:58:10 
 */
public class AkkaServiceClusterSystem implements ServiceClusterSystem, MemberStatusListener {
	private static final String MONITOR_ACTOR_NAME = "monitor";
	private static final String MONITOR_ACTOR_PATH = "/user/" + MONITOR_ACTOR_NAME;
	private static final String FOCUS_TOPIC_KEY = "focus_topic";
	private static final String NODE_INFORMATION_INQUIRE_COMMAND = "node_information_inquire";
	private static final String NODE_RESOURCE_INQUIRE_COMMAND = "node_resource_inquire";
	private static final Logger logger = LoggerFactory.getLogger(AkkaServiceClusterSystem.class);
	private ActorSystem system;
	private ActorShareSystem actorShareSystem;
	private ActorRef mediator;
	private Map<String, AkkaServiceClusterRef> serviceClusterRefs = new ConcurrentHashMap<>();
	private Map<ActorRef, AkkaServiceRef> actorRefMapping = new ConcurrentHashMap<>();
	private Set<ServiceStatusListener> serviceStatusListenerSet = new CopyOnWriteArraySet<>();
	private Set<MemberStatusListener> memberStatusListenerSet = new CopyOnWriteArraySet<>();
	private Map<String, AkkaServiceRef> memberMonitorRefs = new ConcurrentHashMap<>();

	/**共享actor名称,从0开始.**/
	private AtomicInteger actorNameGenerator = new AtomicInteger(0);
	private AtomicInteger askActorNameGenerator = new AtomicInteger(0);

	/**
	 * 服务系统构造
	 * @param name 系统名称
	 * @param config 配置信息
	 */
	public AkkaServiceClusterSystem(String name, Config config) {
		system = ActorSystem.create(name, config);
		mediator = DistributedPubSub.get(system).mediator();
		actorShareSystem = ActorShareSystem.getSystem(system, this::handleActorShare, this);
		createMonitorActor();
	}

	void handleActorShare(List<ActorShare> actorShareList, ClusterStatus s) {
		List<ServiceRef> serviceRefs = new ArrayList<>(actorShareList.size());
		for (ActorShare ref : actorShareList) {
			if (ClusterStatus.UP == s) {
				String shareName = ref.getName();
				ActorRef actorRef = ref.getActorRef();
				AkkaServiceRef akkaServiceRef = createServiceRef(actorRef, shareName);
				actorRefMapping.put(actorRef, akkaServiceRef);
				getAndCreateServiceClusterRef(shareName).addServiceRef(akkaServiceRef);
				serviceRefs.add(akkaServiceRef);
			} else {
				AkkaServiceClusterRef clusterRef = serviceClusterRefs.get(ref.getName());
				if (clusterRef == null) {
					return;
				}
				AkkaServiceRef serviceRef = actorRefMapping.remove(ref.getActorRef());
				if (serviceRef == null) {
					return;
				}
				clusterRef.removeServiceRef(serviceRef);
				system.stop(serviceRef.askPatternActorRef());
				serviceRefs.add(serviceRef);
			}
		}
		notifyServiceStatusListener(serviceRefs, ClusterStatus.UP == s ? Status.ACTIVE : Status.INACTIVE);
	}

	/**
	 * 根据actorRef获取对应服务引用
	 */
	public AkkaServiceRef resolveRef(ActorRef ref) {
		if (Objects.isNull(ref)) {
			return null;
		}
		AkkaServiceRef akkaServiceRef = actorRefMapping.get(ref);
		return Objects.nonNull(akkaServiceRef) ? akkaServiceRef
				: new ResponseOnlyServiceRef(ref, "ResponseOnlyServiceRef");
	}

	@Override
	public void subscribe(String topic, ServiceRef ref) {
		if (!ref.hasLocalScope()) {
			throw new RuntimeException("Subscriber do not allowed remote ServiceRef");
		}
		AkkaServiceRef akkaServiceRef = (AkkaServiceRef) ref;
		ActorRef actorRef = akkaServiceRef.destination();
		//关注
		mediator.tell(new DistributedPubSubMediator.Subscribe(topic, actorRef), actorRef);
		akkaServiceRef.getOrSet(FOCUS_TOPIC_KEY, ArrayList::new).add(topic);
	}

	@Override
	public void publish(String topic, Object msg, ServiceRef sender) {
		AkkaServiceRef akkaServiceRef = (AkkaServiceRef) sender;
		ActorRef actorRef = ActorRef.noSender();
		if (akkaServiceRef != null) {
			actorRef = akkaServiceRef.destination();
		}
		mediator.tell(new DistributedPubSubMediator.Publish(topic, new SubscribeMessage(topic, msg)), actorRef);
	}

	@Override
	public ServiceRef serviceFor(Service service, String name) {
		return serviceFor(service, name, false);
	}

	@Override
	public ServiceRef serviceFor(Service service, String name, boolean local) {
		return CompletableFutureUtil.getSilently(serviceForAsync(service, name, local));
	}

	@Override
	public CompletableFuture<ServiceRef> serviceForAsync(Service service, String name, boolean local) {
		MessageProcessorChain chain = new MessageProcessorChain(new ServiceContextBindingProcessor(this),
				new FutureMessageProcessor(), new AskPatternMessageProcessor(), new ServiceInvokeProcessor(service));
		ActorRef ref = system.actorOf(Props.create(ProcessorChainActor.class, chain),
				String.valueOf(actorNameGenerator.getAndIncrement()));
		return actorShareSystem.share(ref, name, local).thenApply(v -> actorRefMapping.get(ref));
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

	@Override
	public void stop(ServiceRef serviceRef) {
		Objects.requireNonNull(serviceRef);
		AkkaServiceRef ref = (AkkaServiceRef) serviceRef;
		List<String> focusTopics = ref.getOrDefault(FOCUS_TOPIC_KEY, Collections.emptyList());
		focusTopics.forEach(
				e -> mediator.tell(new DistributedPubSubMediator.Unsubscribe(e, ref.destination()), ref.destination()));
		system.stop(ref.destination());
	}

	@Override
	public boolean addServiceStatusListener(ServiceStatusListener listener) {
		return serviceStatusListenerSet.add(listener);
	}

	@Override
	public boolean removeServiceStatusListener(ServiceStatusListener listener) {
		return serviceStatusListenerSet.remove(listener);
	}

	private AkkaServiceClusterRef getAndCreateServiceClusterRef(String name) {
		AkkaServiceClusterRef ref = serviceClusterRefs.get(name);
		if (Objects.isNull(ref)) {
			ref = new AkkaServiceClusterRef(name);
			serviceClusterRefs.put(name, ref);
		}
		return ref;
	}

	private void notifyServiceStatusListener(List<ServiceRef> ref, Status status) {
		for (ServiceStatusListener listener : serviceStatusListenerSet) {
			try {
				listener.handle(ref, status);
			} catch (Throwable ex) {
				logger.error("notify service status listener error {}", ex);
			}
		}
	}

	@Override
	public void focusNormalService(String name) {
		actorShareSystem.actorShareCenter().focusNormalService(name);
	}

	@Override
	public void focusDynamicService(String name, String sequence) {
		actorShareSystem.actorShareCenter().focusDynamicService(name, sequence);
	}

	@Override
	public void focusSpecial(String name) {
		actorShareSystem.actorShareCenter().focusSpecial(name);
	}

	@Override
	public Set<String> normalFocus() {
		return actorShareSystem.actorShareCenter().normalFocus();
	}

	@Override
	public Set<String> dynamicFocus() {
		return actorShareSystem.actorShareCenter().dynamicFocus();
	}

	@Override
	public Set<String> specialFocus() {
		return actorShareSystem.actorShareCenter().specialFocus();
	}

	@Override
	public Set<String> supplys() {
		return actorShareSystem.actorShareCenter().supplys();
	}

	@Override
	public Set<ClusterMember> members() {
		return actorShareSystem.members().stream().map(m -> new ClusterMember(m.address().toString()))
				.collect(Collectors.toSet());
	}

	@Override
	public ClusterMember leader() {
		Option<Address> leader = Cluster.get(system).state().leader();
		return leader.isEmpty() ? null : new ClusterMember(leader.get().toString());
	}

	@Override
	public boolean addMemberStatusListener(MemberStatusListener listener) {
		return memberStatusListenerSet.add(listener);
	}

	@Override
	public boolean removeMemberStatusListener(MemberStatusListener listener) {
		return memberStatusListenerSet.remove(listener);
	}

	@Override
	public void handle(ClusterMember member, Status status) {
		String address = member.getAddress();
		if (Objects.equals(Status.ACTIVE, status)) {
			ActorSelection selection = system.actorSelection(address + MONITOR_ACTOR_PATH);
			selection.resolveOneCS(FiniteDuration.apply(30, TimeUnit.SECONDS)).whenComplete((ref, e) -> {
				if (Objects.nonNull(e)) {
					logger.warn("create remote {}  monitor ref exception", address, e);
					return;
				}
				memberMonitorRefs.put(address, createServiceRef(ref, MONITOR_ACTOR_NAME));
			});
		} else {
			memberMonitorRefs.remove(address);
		}
		//dispatcher event
		memberStatusListenerSet.forEach(e -> e.handle(member, status));
	}

	@Override
	public CompletableFuture<ClusterMemberServiceMessage> inquireInformation(String address) {
		return inquireCommand(address, NODE_INFORMATION_INQUIRE_COMMAND);
	}

	@Override
	public CompletableFuture<ClusterMemberServiceMessage> inquireResource(String address) {
		return inquireCommand(address, NODE_RESOURCE_INQUIRE_COMMAND);
	}

	private <T> CompletableFuture<T> inquireCommand(String address, String command) {
		AkkaServiceRef ref = memberMonitorRefs.get(address);
		if (ref == null) {
			String msg = "member monitor ref not ready address is " + address + " command is " + command;
			logger.warn(msg);
			throw new RuntimeException(msg);
		}
		return ref.ask(command);
	}

	private void createMonitorActor() {
		MessageProcessorChain chain = new MessageProcessorChain(new AskPatternMessageProcessor(), (ctx, msg) -> {
			Object reply = null;
			//处理节点数据查询请求
			if (Objects.equals(msg, NODE_INFORMATION_INQUIRE_COMMAND)) {
				reply = new ClusterMemberServiceMessage(supplys(), normalFocus(), specialFocus());
			}
			//查询当前节点资源信息
			else if (Objects.equals(msg, NODE_RESOURCE_INQUIRE_COMMAND)) {
				reply = populateResource();
			}
			return reply;
		});
		system.actorOf(Props.create(ProcessorChainActor.class, chain), MONITOR_ACTOR_NAME);
	}

	private ClusterMemberResourceMessage populateResource() {
		ClusterMemberResourceMessage result = new ClusterMemberResourceMessage();
		Runtime runtime = Runtime.getRuntime();
		result.setAvailableProcessors(runtime.availableProcessors());
		result.setVmFreeMemroy(runtime.freeMemory());
		result.setVmTotalMemory(runtime.totalMemory());
		ThreadMXBean threadMxbean = ManagementFactory.getThreadMXBean();
		result.setVmThreadCount(threadMxbean.getThreadCount());
		result.setVmDaemonThreadCount(threadMxbean.getDaemonThreadCount());
		return result;
	}

	private AkkaServiceRef createServiceRef(ActorRef des, String name) {
		MessageProcessorChain chain = new MessageProcessorChain(new AskPatternMessageProcessor());
		ActorRef askPatternRef = system.actorOf(Props.create(ProcessorChainActor.class, chain),
				"ask" + askActorNameGenerator.getAndIncrement());
		AkkaServiceRef akkaServiceRef = new AkkaServiceRef(des, name, askPatternRef);
		return akkaServiceRef;
	}

}