package org.appsugar.cluster.service.akka.share;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.akka.domain.ActorClusterShareMessage;
import org.appsugar.cluster.service.akka.domain.ActorShare;
import org.appsugar.cluster.service.akka.domain.ClusterStatus;
import org.appsugar.cluster.service.akka.domain.FocusMessage;
import org.appsugar.cluster.service.akka.domain.LocalShareMessage;
import org.appsugar.cluster.service.akka.util.DynamicServiceUtils;
import org.appsugar.cluster.service.api.Focusable;
import org.appsugar.cluster.service.api.MemberStatusListener;
import org.appsugar.cluster.service.domain.ClusterMember;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
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
public class ActorShareCenter implements ClusterMemberListener, ActorShareListener, Focusable {

	public static final String ACTOR_SHARE_COLLECTOR_NAME = "cluster_share";
	public static final String ACTOR_SHARE_COLLECTOR_PATH = "/user/" + ACTOR_SHARE_COLLECTOR_NAME;
	private static final Logger logger = LoggerFactory.getLogger(ActorShareCenter.class);

	private Set<Member> members = new HashSet<>();
	private Map<Address, MemberInformation> remoteActorRef = new HashMap<>();
	private List<ActorShare> localActorRefList = new ArrayList<>();
	private ActorSystem system;
	private ActorRef shareCollectorRef;
	private ActorShareListener actorShareListener;
	private Address selfAddress;
	/**节点监听器**/
	private MemberStatusListener memberStatusListener;
	/**普通关注列表**/
	private Set<String> normalFocus = ConcurrentHashMap.newKeySet();
	/**动态关注列表**/
	private Set<String> dynamicFocus = ConcurrentHashMap.newKeySet();
	/**特殊关注列表**/
	private Set<String> specialFocus = ConcurrentHashMap.newKeySet();
	/**动态工厂列表**/
	private Set<String> dynamicFactoryNames = ConcurrentHashMap.newKeySet();

	public ActorShareCenter(ActorSystem system, ActorShareListener actorShareListener,
			MemberStatusListener memberStatusListener) {
		super();
		this.memberStatusListener = memberStatusListener;
		this.system = system;
		this.actorShareListener = actorShareListener;
		shareCollectorRef = system.actorOf(Props.create(ActorShareCollector.class, this, this),
				ACTOR_SHARE_COLLECTOR_NAME);
		this.selfAddress = Cluster.get(system).selfAddress();
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
		//通知本地监听器修改
		try {
			actorShareListener.handle(actors, status);
		} finally {
			ActorShare actorShare = actors.get(0);
			processActorShare(actorShare, status);
		}
	}

	@Override
	public void handle(Member m, ClusterStatus state) {
		Address address = m.address();
		if (isLocalAddress(address)) {
			return;
		}
		logger.info("member event status {} member {}", state, m);
		//派发服务状态消息(不适用当前actor线程执行,防止block)
		if (ClusterStatus.UP.equals(state)) {
			members.add(m);
			information(address);
			fireMemberEvent(m, state);
			ActorSelection as = remoteShareActorSelection(address);
			//节点上线,发送本地所有关心的普通服务到该节点上
			if (!normalFocus.isEmpty()) {
				as.tell(new FocusMessage(normalFocus), shareCollectorRef);
			}
		} else {
			if (!members.remove(m)) {
				//如果member已经移除, 那么不在处理移除事件
				return;
			}
			fireMemberEvent(m, state);
			MemberInformation inf = remoteActorRef.remove(address);
			if (Objects.isNull(inf)) {
				return;
			}
			List<ActorShare> actorShareList = inf.getShareActorList();
			//有可能接收到unreachable 和 memberRemove事件,导致空指针异常
			if (actorShareList.isEmpty()) {
				return;
			}
			//该服务节点被移除,对应的actor共享服务也应该被移除
			actorShareListener.handle(actorShareList, ClusterStatus.DOWN);
			actorShareList.clear();
		}
	}

	protected void fireMemberEvent(Member m, ClusterStatus state) {
		system.dispatcher().execute(() -> memberStatusListener.handle(new ClusterMember(m.address().toString()),
				ClusterStatus.UP.equals(state) ? Status.ACTIVE : Status.INACTIVE));
	}

	protected MemberInformation information(Address address) {
		return MapUtils.getOrCreate(remoteActorRef, address, MemberInformation::new);
	}

	public Set<Member> members() {
		return members;
	}

	@Override
	public void focusNormalService(String name) {
		if (!normalFocus.add(name)) {
			return;
		}
		FocusMessage focusMessage = new FocusMessage(name, false);
		//通知所有节点我关注这个服务
		remoteActorRef.entrySet().stream().forEach(e -> notifyFocusMessage(e.getKey(), focusMessage));
	}

	@Override
	public void focusDynamicService(String name, String sequence) {
		String realName = DynamicServiceUtils.getDynamicServiceNameWithSequence(name, sequence);
		dynamicFactoryNames.add(name);
		if (!dynamicFocus.add(realName)) {
			return;
		}
		FocusMessage focusMessage = new FocusMessage(realName, false);
		//通知服务名为name的所有节点. 我关注这个动态服务
		remoteActorRef.entrySet().stream().filter(e -> e.getValue().supplyOn(name))
				.forEach(e -> notifyFocusMessage(e.getKey(), focusMessage));
	}

	@Override
	public void focusSpecial(String name) {
		if (!specialFocus.add(name)) {
			return;
		}
		FocusMessage focusMessage = new FocusMessage(name, true);
		//通知服务名为name的所有节点. 我关注所有name开头的动态服务
		remoteActorRef.entrySet().stream().filter(e -> e.getValue().supplyOn(name))
				.forEach(e -> notifyFocusMessage(e.getKey(), focusMessage));
	}

	/**
	 * 处理共享actor
	 * @author NewYoung
	 * 2017年5月11日下午5:18:16
	 */
	void processActorShare(ActorShare actorShare, ClusterStatus status) {
		String name = actorShare.getName();
		ActorRef ref = actorShare.getActorRef();
		Address address = ref.path().address();
		//如果是本地actor服务(本地服务一次只会有一个)
		if (isLocalAddress(address)) {
			if (ClusterStatus.UP.equals(status)) {
				localActorRefList.add(actorShare);
			} else {
				localActorRefList.remove(actorShare);
			}
			if (actorShare.isLocal()) {
				return;
			}
			String specialName = DynamicServiceUtils.getDynamicServiceFirstName(name);
			ActorClusterShareMessage msg = new ActorClusterShareMessage(status, new ActorShare(actorShare.getName()));
			//通知关注该服务的member,我本地服务有变化啦
			remoteActorRef.entrySet().stream().filter(e -> e.getValue().focusOn(name)).forEach(e -> {
				if (Objects.nonNull(specialName)) {
					if (e.getValue().focusOnSpecial(specialName)) {
						return;
					}
				}
				ActorSelection as = remoteShareActorSelection(e.getKey());
				as.tell(msg, ref);
			});
			if (Objects.isNull(specialName)) {
				return;
			}
			//动态服务,告知所有动态服务创建节点
			remoteActorRef.entrySet().stream().filter(e -> e.getValue().focusOnSpecial(specialName)).forEach(e -> {
				ActorSelection as = remoteShareActorSelection(e.getKey());
				as.tell(msg, ref);
			});
		} else {
			MemberInformation info = information(address);
			if (ClusterStatus.UP.equals(status)) {
				info.addActorShare(actorShare);
				checkAndNotifyDynamic(name, address);
				checkAndNotifySpecial(name, address);
			} else {
				info.removeActorShare(actorShare);
			}
		}
	}

	/**
	 * 检测并发送动态服务名到指定节点
	 * @author NewYoung
	 * 2017年6月15日下午4:19:40
	 */
	void checkAndNotifyDynamic(String name, Address address) {
		if (!dynamicFactoryNames.contains(name)) {
			return;
		}
		Set<String> dynamicFocusByName = dynamicFocus.stream()
				.filter(e -> DynamicServiceUtils.isDynamicServiceAs(name, e)).collect(Collectors.toSet());
		if (dynamicFocusByName.isEmpty()) {
			return;
		}
		FocusMessage focusMessage = new FocusMessage(dynamicFocusByName, false);
		notifyFocusMessage(address, focusMessage);
	}

	/**
	 * 检测并发送特殊关注服务名到指定节点
	 * @author NewYoung
	 * 2017年6月15日下午4:20:57
	 */
	void checkAndNotifySpecial(String name, Address address) {
		if (!specialFocus.contains(name)) {
			return;
		}
		//告诉当前member,我关注该服务的所有动态服务
		FocusMessage focusMessage = new FocusMessage(name, true);
		notifyFocusMessage(address, focusMessage);
	}

	/**
	 * 通知消息
	 * @author NewYoung
	 * 2017年5月11日下午4:29:53
	 */
	void notifyFocusMessage(Address address, FocusMessage msg) {
		ActorSelection as = remoteShareActorSelection(address);
		as.tell(msg, shareCollectorRef);
	}

	ActorSelection remoteShareActorSelection(Address address) {
		return system.actorSelection(address + ACTOR_SHARE_COLLECTOR_PATH);
	}

	boolean isLocalAddress(Address address) {
		return address.hasLocalScope() ? true : address.equals(selfAddress);
	}

	/**
	 * 节点关注服务处理
	 * @author NewYoung
	 * 2017年5月11日下午4:30:12
	 */
	@Override
	public void memberFocus(ActorRef actor, FocusMessage msg) {
		Address address = actor.path().address();
		MemberInformation inf = information(address);
		Set<String> names = msg.getNames();
		Predicate<ActorShare> p = null;
		if (!msg.isWatchDynamic()) {
			inf.getFocusSet().addAll(names);
			p = a -> names.contains(a.getName()) && !a.isLocal();
		} else {
			inf.getFocusSpecialSet().addAll(names);
			p = a -> names.stream().anyMatch(n -> DynamicServiceUtils.isDynamicServiceAs(n, a.getName()));
		}
		ActorSelection as = remoteShareActorSelection(address);
		//把当前所有该节点关注的name服务告诉给对方
		localActorRefList.stream().filter(p).forEach(e -> as
				.tell(new ActorClusterShareMessage(ClusterStatus.UP, new ActorShare(e.getName())), e.getActorRef()));
	}

	@Override
	public Set<String> normalFocus() {
		return unmodifySet(normalFocus);
	}

	@Override
	public Set<String> dynamicFocus() {
		return unmodifySet(dynamicFocus);
	}

	@Override
	public Set<String> specialFocus() {
		return unmodifySet(specialFocus);
	}

	@Override
	public Set<String> supplys() {
		return localActorRefList.stream().map(ActorShare::getName).collect(Collectors.toSet());
	}

	private <T> Set<T> unmodifySet(Set<T> orignal) {
		return Collections.unmodifiableSet(orignal);
	}
}