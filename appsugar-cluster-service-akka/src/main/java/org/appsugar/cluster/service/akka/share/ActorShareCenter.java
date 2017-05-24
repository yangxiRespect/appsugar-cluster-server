package org.appsugar.cluster.service.akka.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.akka.domain.ActorClusterShareMessage;
import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;
import org.appsugar.cluster.service.akka.domain.LocalShareMessage;
import org.appsugar.cluster.service.api.StatusListener;
import org.appsugar.cluster.service.domain.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Member;

/**
 * actor共享中心
 * 实现节点加入,把本地共享actor发送给加入节点
 * 实现节点退出,通知本地系统节点对应actor退出
 * 把本地actor共享到网络中
 * 处理actor共享关闭事件
 * @author NewYoung
 * 2016年5月27日下午4:31:49
 */
public class ActorShareCenter implements ClusterMemberListener, ActorShareListener {

	public static final String ACTOR_SHARE_COLLECTOR_NAME = "cluster_share";
	public static final String ACTOR_SHARE_COLLECTOR_PATH = "/user/" + ACTOR_SHARE_COLLECTOR_NAME;
	private static final Logger logger = LoggerFactory.getLogger(ActorShareCenter.class);

	private Set<Member> members = new HashSet<>();
	private Map<Address, List<ActorShare>> remoteActorRef = new HashMap<>();
	private List<ActorShare> localActorRefList = new ArrayList<>();
	private ActorSystem system;
	private ActorRef shareCollectorRef;
	private ActorShareListener actorShareListener;
	private StatusListener<org.appsugar.cluster.service.domain.Member> memberListener;

	public ActorShareCenter(ActorSystem system, ActorShareListener actorShareListener,
			StatusListener<org.appsugar.cluster.service.domain.Member> memberListener) {
		super();
		this.memberListener = memberListener;
		this.system = system;
		this.actorShareListener = actorShareListener;
		shareCollectorRef = system.actorOf(Props.create(ActorShareCollector.class, this, this),
				ACTOR_SHARE_COLLECTOR_NAME);
	}

	/**
	 * 共享指定actor
	 */
	public CompletableFuture<Void> share(ActorRef ref, String name, boolean local) {
		logger.debug("prepare share local actor ref {}", ref);
		if (!ref.path().address().hasLocalScope()) {
			throw new RuntimeException("actor ref not local  " + ref);
		}
		CompletableFuture<Void> future = new CompletableFuture<>();
		//逻辑交给共享actor收集器处理
		shareCollectorRef.tell(new LocalShareMessage(name, ref, future, local), ActorRef.noSender());
		return future;
	}

	/**
	 * 共享指定actor
	 */
	public CompletableFuture<Void> share(ActorRef ref, String name) {
		return share(ref, name, false);
	}

	/**
	 * actors 只会一次一个
	 */
	@Override
	public void handle(List<ActorShare> actors, ClusterStatus status) {
		logger.debug(" share actor event  status {} share {}", status, actors);
		//通知本地监听器修改
		try {
			actorShareListener.handle(actors, status);
		} finally {
			ActorShare actorShare = actors.get(0);
			//如果是本地actor服务(本地服务一次只会有一个)
			if (actorShare.getActorRef().path().address().hasLocalScope()) {
				if (ClusterStatus.UP.equals(status)) {
					localActorRefList.addAll(actors);
				} else {
					localActorRefList.removeAll(actors);
				}
				if (actorShare.isLocal()) {
					return;
				}
				//告诉所有member,本地actorref 有改变了 
				members.stream()
						.forEach(m -> system.actorSelection(m.address() + ACTOR_SHARE_COLLECTOR_PATH).tell(
								new ActorClusterShareMessage(status, new ActorShare(actorShare.getName())),
								actorShare.getActorRef()));
			} else {
				List<ActorShare> remoteActorList = getAndCreateShareActorCollection(
						actorShare.getActorRef().path().address());
				if (ClusterStatus.UP.equals(status)) {
					remoteActorList.add(actorShare);
				} else {
					remoteActorList.remove(actorShare);
				}
			}
		}
	}

	@Override
	public void handle(Member m, ClusterStatus state) {
		ActorSelection as = system.actorSelection(m.address() + ACTOR_SHARE_COLLECTOR_PATH);
		if (as.anchorPath().address().hasLocalScope()) {
			logger.debug("self member event  do nothing");
			return;
		}
		logger.info("member event status {} member {}", state, m);
		if (ClusterStatus.UP.equals(state)) {
			members.add(m);
			getAndCreateShareActorCollection(m.address());
			if (localActorRefList.isEmpty()) {
				return;
			}
			memberListener.handle(new org.appsugar.cluster.service.domain.Member(m.address().toString()),
					Status.ACTIVE);
			logger.debug("send local share actor to member {}  actor address {}", m.address(),
					as.anchorPath().address());
			//把本地所有非局部共享actor发送给对应节点
			localActorRefList.stream().filter(e -> !e.isLocal())
					.forEach(l -> as.tell(new ActorClusterShareMessage(ClusterStatus.UP, new ActorShare(l.getName())),
							l.getActorRef()));
		} else {
			if (!members.remove(m)) {
				return;
			}
			memberListener.handle(new org.appsugar.cluster.service.domain.Member(m.address().toString()),
					Status.INACTIVE);
			List<ActorShare> actorShareList = remoteActorRef.remove(m.address());
			//有可能接收到unreachable 和 memberRemove事件,导致空指针异常
			if (Objects.isNull(actorShareList) || actorShareList.isEmpty()) {
				return;
			}
			//该服务节点被移除,对应的actor共享服务也应该被移除
			actorShareListener.handle(actorShareList, ClusterStatus.DOWN);
			actorShareList.clear();
		}
	}

	protected List<ActorShare> getAndCreateShareActorCollection(Address address) {
		List<ActorShare> actorShareList = remoteActorRef.get(address);
		if (actorShareList == null) {
			actorShareList = new ArrayList<>();
			remoteActorRef.put(address, actorShareList);
		}
		return actorShareList;
	}

	public Set<Member> members() {
		return members;
	}
}