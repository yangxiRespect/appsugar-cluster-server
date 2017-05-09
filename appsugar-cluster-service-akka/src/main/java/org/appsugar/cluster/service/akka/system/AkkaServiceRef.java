package org.appsugar.cluster.service.akka.system;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.appsugar.cluster.service.akka.domain.AskPatternEvent;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.ServiceContextUtil;

import akka.actor.ActorRef;
import akka.actor.Address;
import scala.Option;

/**
 * akka服务引用
 * @author NewYoung
 * 2016年5月29日下午4:31:57
 */
public class AkkaServiceRef implements ServiceRef, Comparable<AkkaServiceRef> {

	//默认三十秒超时
	public static final long defaultTimeout = 1000 * 30;
	//目的地引用
	private ActorRef destination;
	//服务名称
	private String name;
	//用于askPattern引用
	private ActorRef askPatternRef;
	/**系统名称**/
	private String system;

	private Address address;
	/**附件信息**/
	private Map<Object, Object> attachments = new ConcurrentHashMap<>();

	public AkkaServiceRef(ActorRef destination, String name) {
		this(destination, name, null);
	}

	public AkkaServiceRef(ActorRef destination, String name, ActorRef askPatternRef) {
		super();
		this.destination = destination;
		this.name = name;
		this.askPatternRef = askPatternRef;
		this.address = destination.path().address();
		this.system = this.address.system();
	}

	@Override
	public <T> CompletableFuture<T> ask(Object msg) {
		return ask(msg, defaultTimeout);
	}

	@Override
	public <T> CompletableFuture<T> ask(Object msg, long timeout) {
		if (timeout < 1l) {
			throw new IllegalArgumentException("timeout mush greater than 0");
		}
		CompletableFuture<T> future = new CompletableFuture<>();
		AkkaServiceContext context = (AkkaServiceContext) ServiceContextUtil.context();
		if (Objects.nonNull(context)) {
			ServiceRef self = context.self();
			//处理非同一系统请求
			if (!((AkkaServiceRef) self).system.equals(this.system)) {
				//把消息交由askPatterRef去请求.
				askPatternRef.tell(new AskPatternEvent<>(msg, future, timeout, destination), ActorRef.noSender());
				return CompletableFutureUtil.wrapContextFuture(future);
			}
			//解决多次tell性能问题
			ProcessorContext pctx = context.getAttribute(ServiceContextBindingProcessor.PROCESSOR_CONTEXT_KEY);
			try {
				pctx.processNext(new AskPatternEvent<>(msg, future, timeout, destination));
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
			return future;
		}
		AskPatternEvent<T> event = new AskPatternEvent<>(msg, future, timeout, destination);
		askPatternRef.tell(event, ActorRef.noSender());
		return future;
	}

	@Override
	public void tell(Object msg, ServiceRef sender) {
		ActorRef actorSender = ActorRef.noSender();
		if (sender != null && sender instanceof AkkaServiceRef) {
			actorSender = ((AkkaServiceRef) sender).destination;
		}
		destination.tell(msg, actorSender);
	}

	@Override
	public boolean hasLocalScope() {
		return destination.path().address().hasLocalScope();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return destination.path().toString();
	}

	@Override
	public Optional<String> host() {
		Option<String> s = destination.path().address().host();
		return s.isEmpty() ? Optional.empty() : Optional.of(s.get());
	}

	@Override
	public String hostPort() {
		return destination.path().address().hostPort();
	}

	@Override
	public int hashCode() {
		return destination.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AkkaServiceRef other = (AkkaServiceRef) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AkkaServiceRef [destination=").append(destination).append(", name=").append(name).append("]");
		return builder.toString();
	}

	ActorRef destination() {
		return destination;
	}

	ActorRef askPatternActorRef() {
		return askPatternRef;
	}

	@Override
	public int compareTo(AkkaServiceRef o) {
		return o == null ? 1 : this.destination.path().toString().compareTo(o.destination.path().toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> V getOrDefault(K key, V defaultValue) {
		return (V) attachments.getOrDefault(key, defaultValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> V get(K key) {
		return (V) attachments.get(key);
	}

	@Override
	public <K, V> V attach(K key, V value) {
		attachments.put(key, value);
		return value;
	}

	@Override
	public boolean isSameAddress(ServiceRef ref) {
		AkkaServiceRef des = (AkkaServiceRef) ref;
		return this.destination.path().address().equals(des.destination.path().address());
	}

}