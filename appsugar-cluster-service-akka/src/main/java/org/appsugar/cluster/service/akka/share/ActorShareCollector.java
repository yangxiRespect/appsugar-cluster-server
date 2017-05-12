package org.appsugar.cluster.service.akka.share;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.akka.domain.ActorClusterShareMessage;
import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;
import org.appsugar.cluster.service.akka.domain.FocusMessage;
import org.appsugar.cluster.service.akka.domain.LocalShareMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;

/**
 * actor共享收集器
 * 接收来自其他节点的共享actor
 * 接收其他节点事件
 * 实现本地actor共享并watch
 * @author NewYoung
 * 2016年5月27日下午4:30:53
 */
public class ActorShareCollector extends AbstractActor {

	public static final Logger logger = LoggerFactory.getLogger(ActorShareCollector.class);

	public static final String AUTO_DOWN_UNREACHEABLE_KEY = "akka.actor.share.auto-down";

	private ClusterMemberListener memberListener;
	private ActorShareListener actorShareListener;
	private Set<ActorRef> watchList = new HashSet<>();
	private Map<ActorRef, ActorShare> localShareMapping = new HashMap<>();
	private Cluster cluster = Cluster.get(getContext().system());
	private List<Class<?>> clusterSubscribeTypeList = Arrays.asList(MemberUp.class, MemberRemoved.class,
			UnreachableMember.class, ReachableMember.class);
	private boolean autoDown = false;

	public ActorShareCollector(ClusterMemberListener memberListener, ActorShareListener actorShareListener) {
		super();
		this.memberListener = memberListener;
		this.actorShareListener = actorShareListener;
		Config config = getContext().system().settings().config();
		if (config.hasPath(AUTO_DOWN_UNREACHEABLE_KEY)) {
			autoDown = config.getBoolean(AUTO_DOWN_UNREACHEABLE_KEY);
		}
		logger.debug("Cluster member auto down is {}", autoDown);
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		clusterSubscribeTypeList.stream().forEach(c -> cluster.subscribe(self(), c));
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		clusterSubscribeTypeList.stream().forEach(c -> cluster.unsubscribe(self(), c));
		watchList.stream().forEach(w -> getContext().unwatch(w));
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().matchAny(msg -> {
			try {
				//处理集群节点事件
				if (msg instanceof MemberUp) {
					memberListener.handle(((MemberUp) msg).member(), ClusterStatus.UP);
				} else if (msg instanceof MemberRemoved) {
					Member member = ((MemberRemoved) msg).member();
					memberListener.handle(member, ClusterStatus.DOWN);
				} else if (msg instanceof UnreachableMember) {
					Member member = ((UnreachableMember) msg).member();
					memberListener.handle(member, ClusterStatus.DOWN);
					if (autoDown) {
						whenMemberDown(member);
					}
				} else if (msg instanceof ReachableMember) {
					memberListener.handle(((ReachableMember) msg).member(), ClusterStatus.UP);
				}
				//处理共享actor消息
				else if (msg instanceof ActorClusterShareMessage) {
					ActorClusterShareMessage shareMessage = (ActorClusterShareMessage) msg;
					shareMessage.getShare().setActorRef(sender());
					actorShareListener.handle(Arrays.asList(shareMessage.getShare()), shareMessage.getStatus());
				}
				//处理服务关注消息
				else if (msg instanceof FocusMessage) {
					actorShareListener.memberFocus(sender(), (FocusMessage) msg);
				}
				//处理本地共享消息
				else if (msg instanceof LocalShareMessage) {
					processLocalShareMessage((LocalShareMessage) msg);
				} //处理本地共享actor关闭事件
				else if (msg instanceof Terminated) {
					ActorRef ref = sender();
					getContext().unwatch(ref);
					ActorShare actorShare = localShareMapping.remove(ref);
					actorShareListener.handle(Arrays.asList(actorShare), ClusterStatus.DOWN);
				}
			} catch (Throwable ex) {
				logger.error("process akka cluster member event error {}", ex);
			}
		}).build();
	}

	protected void whenMemberDown(Member member) {
		logger.info("manually leave member {}", member.address());
		cluster.down(member.address());
	}

	/**
	 * 处理来自本地共享请求
	 */
	void processLocalShareMessage(LocalShareMessage msg) {
		CompletableFuture<Void> future = msg.getFuture();
		ActorRef ref = msg.getRef();
		if (watchList.contains(ref)) {
			logger.warn("current actor ref already shared {}", msg);
			future.completeExceptionally(new RuntimeException("current actor ref already shared " + msg));
			return;
		}
		String name = msg.getName();
		ActorShare actorShare = new ActorShare(name, msg.isLocal());
		actorShare.setActorRef(ref);
		try {
			actorShareListener.handle(Arrays.asList(actorShare), ClusterStatus.UP);
			watchList.add(ref);
			localShareMapping.put(ref, actorShare);
			getContext().watch(ref);
			future.complete(null);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
			watchList.remove(ref);
			localShareMapping.remove(ref);
			throw ex;
		}
	}

}