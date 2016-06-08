package org.appsugar.cluster.service.akka.system;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.appsugar.cluster.service.api.FutureMessage;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;
import org.appsugar.cluster.service.api.ServiceRef;

import akka.actor.ActorRef;
import scala.Option;

/**
 * akka服务引用
 * @author NewYoung
 * 2016年5月29日下午4:31:57
 */
public class AkkaServiceRef implements ServiceRef {
	//默认五分钟超时
	public static final long defaultTimeout = 1000 * 60 * 5;
	//目的地引用
	private ActorRef destination;
	//服务名称
	private String name;
	//用于askPattern引用
	private ActorRef askPatternRef;

	public AkkaServiceRef(ActorRef destination, String name) {
		super();
		this.destination = destination;
		this.name = name;
	}

	public AkkaServiceRef(ActorRef destination, String name, ActorRef askPatternRef) {
		super();
		this.destination = destination;
		this.name = name;
		this.askPatternRef = askPatternRef;
	}

	@Override
	public <T> T ask(Object msg) {
		return ask(msg, defaultTimeout);
	}

	@Override
	public <T> T ask(Object msg, long timeout) {
		if (timeout < 1l) {
			throw new IllegalArgumentException("timeout mush greater than 0");
		}
		CompletableFuture<T> future = new CompletableFuture<T>();
		AskPatternEvent<T> event = new AskPatternEvent<>(msg, future, timeout, destination);
		askPatternRef.tell(event, ActorRef.noSender());
		try {
			return future.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error) {
		ask(msg, success, error, defaultTimeout);
	}

	@Override
	public <T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error, long timeout) {
		if (timeout < 1l) {
			throw new IllegalArgumentException("timeout mush greater than 0");
		}
		BiConsumer<T, Throwable> consumer = (r, e) -> {
			if (e != null) {
				error.accept(e);
				return;
			}
			success.accept(r);
		};
		AkkaServiceContext context = (AkkaServiceContext) ServiceContextThreadLocal.context();
		ActorRef sender = askPatternRef;
		if (context != null) {
			//如果在服务执行上下文中, 那么该请求转发至该服务中处理
			sender = context.self().destination;
			//处理非同一系统请求
			if (!sender.path().address().system().equals(destination.path().address().system())) {
				CompletableFuture<T> middleware = new CompletableFuture<>();
				middleware.whenComplete((r, e) -> {
					//把结果转发到context.self
					ActorRef self = context.self().destination;
					self.tell(new FutureMessage<T>(r, e, consumer), destination);
				});
				//把消息交由askPatterRef去请求.
				askPatternRef.tell(new AskPatternEvent<>(msg, middleware, timeout, destination), ActorRef.noSender());
				return;
			}
			//解决多次tell性能问题
			CompletableFuture<T> future = new CompletableFuture<T>();
			future.whenComplete(consumer);
			ProcessorContext pctx = context.getAttribute(ServiceContextBindingProcessor.PROCESSOR_CONTEXT_KEY);
			try {
				pctx.processNext(new AskPatternEvent<>(msg, future, timeout, destination));
			} catch (Throwable e1) {
			}
			return;
		}
		CompletableFuture<T> future = new CompletableFuture<T>();
		future.whenComplete(consumer);
		AskPatternEvent<T> event = new AskPatternEvent<>(msg, future, timeout, destination);
		sender.tell(event, ActorRef.noSender());
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		return result;
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
}