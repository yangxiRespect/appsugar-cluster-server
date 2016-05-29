package org.appsugar.cluster.service.akka.share;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;

/**
 * actor共享收集器
 * 接收来自其他节点的共享actor
 * 接收其他节点事件
 * 实现本地actor共享并watch
 * @author NewYoung
 * 2016年5月27日下午4:30:53
 */
public class ActorShareCollector extends UntypedActor {

	public static final Logger logger = LoggerFactory.getLogger(ActorShareCollector.class);
	private ClusterMemberListener memberListener;
	private ActorShareListener actorShareListener;
	private Set<ActorRef> watchList = new HashSet<>();
	private Map<ActorRef, ActorShare> localShareMapping = new HashMap<>();
	private Cluster cluster = Cluster.get(getContext().system());
	private List<Class<?>> clusterSubscribeTypeList = Arrays.asList(MemberUp.class, MemberRemoved.class,
			UnreachableMember.class);

	public ActorShareCollector(ClusterMemberListener memberListener, ActorShareListener actorShareListener) {
		super();
		this.memberListener = memberListener;
		this.actorShareListener = actorShareListener;
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		clusterSubscribeTypeList.stream().forEach(c -> cluster.subscribe(getSelf(), c));
	}

	@Override
	public void postStop() throws Exception {
		super.postStop();
		clusterSubscribeTypeList.stream().forEach(c -> cluster.unsubscribe(getSelf(), c));
		watchList.stream().forEach(w -> getContext().unwatch(w));
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		try {
			//处理集群节点事件
			if (msg instanceof MemberUp) {
				memberListener.handle(((MemberUp) msg).member(), ClusterStatus.UP);
			} else if (msg instanceof MemberRemoved) {
				memberListener.handle(((MemberRemoved) msg).member(), ClusterStatus.DOWN);
			} else if (msg instanceof UnreachableMember) {
				memberListener.handle(((UnreachableMember) msg).member(), ClusterStatus.DOWN);
			} //处理共享actor消息
			else if (msg instanceof ActorClusterShareMessage) {
				ActorClusterShareMessage shareMessage = (ActorClusterShareMessage) msg;
				shareMessage.getShare().setActorRef(getSender());
				actorShareListener.handle(Arrays.asList(shareMessage.getShare()), shareMessage.getStatus());
			} //处理本地共享消息
			else if (msg instanceof LocalShareMessage) {
				processLocalShareMessage((LocalShareMessage) msg);
			} //处理本地共享actor关闭事件
			else if (msg instanceof Terminated) {
				ActorRef ref = getSender();
				getContext().unwatch(ref);
				ActorShare actorShare = localShareMapping.remove(ref);
				actorShareListener.handle(Arrays.asList(actorShare), ClusterStatus.DOWN);
			}
		} catch (Throwable ex) {
			logger.error("process akka cluster member event error {}", ex);
		}
	}

	/**
	 * 处理来自本地共享请求
	 */
	private void processLocalShareMessage(LocalShareMessage msg) {
		CompletableFuture<Boolean> future = msg.getFuture();
		ActorRef ref = msg.getRef();
		if (watchList.contains(ref)) {
			logger.warn("current actor ref already shared {}", msg);
			future.complete(Boolean.FALSE);
			return;
		}
		String name = msg.getName();
		ActorShare actorShare = new ActorShare(name);
		actorShare.setActorRef(ref);
		try {
			actorShareListener.handle(Arrays.asList(actorShare), ClusterStatus.UP);
			watchList.add(ref);
			localShareMapping.put(ref, actorShare);
			getContext().watch(ref);
			future.complete(Boolean.TRUE);
		} catch (Exception ex) {
			future.completeExceptionally(ex);
			watchList.remove(ref);
			localShareMapping.remove(ref);
			throw ex;
		}
	}

}