package org.appsugar.cluster.service.binding;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.appsugar.cluster.service.api.DistributionRPCSystem;
import org.appsugar.cluster.service.api.DynamicServiceFactory;
import org.appsugar.cluster.service.api.Service;
import org.appsugar.cluster.service.api.ServiceClusterRef;
import org.appsugar.cluster.service.api.ServiceClusterSystem;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.domain.CommandMessage;
import org.appsugar.cluster.service.domain.DynamicServiceCreateMessage;
import org.appsugar.cluster.service.domain.DynamicServiceRequest;
import org.appsugar.cluster.service.domain.ServiceStatusMessage;
import org.appsugar.cluster.service.domain.Status;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.cluster.service.util.RPCSystemUtil;
import org.appsugar.cluster.service.util.ServiceContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态创建服务
 * @author NewYoung
 * 2016年6月14日下午2:45:45
 */
public class DynamicCreatorService implements Service {
	private static final Logger logger = LoggerFactory.getLogger(DynamicCreatorService.class);
	public static final String SERVICE_BALANCE_KEY = "service_balance";
	public static final String SERVICE_ALREADY_EXISTS = "service already exists";

	private DynamicServiceFactory factory;

	private ServiceClusterSystem system;

	private DistributionRPCSystem rpcSystem;

	private ServiceDocker<Object, ServiceCreateParam> docker = new ServiceDocker<>(this::createService,
			p -> p.sequence);

	private Set<String> createdServices = new HashSet<>();

	/**服务名称**/
	private String name;

	public DynamicCreatorService(DynamicServiceFactory factory, ServiceClusterSystem system,
			DistributionRPCSystem rpcSystem, String name) {
		super();
		this.factory = factory;
		this.system = system;
		this.rpcSystem = rpcSystem;
		this.name = name;
	}

	@Override
	public Object handle(Object msg, ServiceContext context) throws Exception {
		if (msg instanceof DynamicServiceRequest) {
			return handleDynamicServiceRequest((DynamicServiceRequest) msg, context);
		} else if (msg instanceof DynamicServiceCreateMessage) {
			return handleDynamicServiceCreateMessage((DynamicServiceCreateMessage) msg);
		} else if (msg instanceof ServiceStatusMessage) {
			handleServiceStatusMessage((ServiceStatusMessage) msg);
		} else if (msg instanceof CommandMessage) {
			CommandMessage cmd = (CommandMessage) msg;
			if (Objects.equals(CommandMessage.CLOSE_COMMAND, cmd.getCmd())) {
				logger.info("prepar to stop self manully self {}    sender {}", context.self(), context.sender());
				context.system().stop(context.self());
			} else if (Objects.equals(CommandMessage.QUERY_DYNAMIC_SERVICE_COMMAND, cmd.getCmd())) {
				String sequence = cmd.getCmd();
				return createdServices.contains(sequence);
			}
		}
		return null;
	}

	/**
	 * 处理服务失效 
	 */
	protected void handleServiceStatusMessage(ServiceStatusMessage msg) {
		ServiceRef ref = msg.getServiceRef();
		String sequence = RPCSystemUtil.getDynamicServiceSequenceByName(name, ref.name());
		if (Objects.isNull(sequence)) {
			return;
		}
		int balanceAdjustment = 1;
		if (Objects.equals(Status.ACTIVE, msg.getStatus())) {
			checkSameNameServiceIfNescessary(ref);
			if (!createdServices.add(sequence)) {
				//服务已存在.不重复添加
				return;
			}
		} else {
			createdServices.remove(sequence);
			balanceAdjustment = -1;
		}
		adjustDynamicServiceBalance(ref, balanceAdjustment);
	}

	/**
	 * 处理服务创建消息
	 */
	protected CompletableFuture<Void> handleDynamicServiceCreateMessage(DynamicServiceCreateMessage msg)
			throws Exception {
		String sequence = msg.getSequence();
		String expectedServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence);
		ServiceClusterRef expectedServiceClusterRef = system.serviceOf(expectedServiceName);
		if (Objects.nonNull(expectedServiceClusterRef) && expectedServiceClusterRef.size() != 0) {
			return CompletableFuture.completedFuture(null);
		}
		//异步创建出服务者
		return CompletableFutureUtil.wrapContextFuture(factory.create(msg.getSequence()).thenCompose(
				r -> rpcSystem.serviceForAsync(r, RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence))));
	}

	/**
	 * 处理创建请求
	 */
	protected Object handleDynamicServiceRequest(DynamicServiceRequest msg, ServiceContext ctx) throws Exception {
		ServiceClusterRef clusterRef = system.serviceOf(name);
		String sequence = msg.getSequence();
		//如果该服务已经创建成功直接返回
		if (createdServices.contains(sequence)) {
			return SERVICE_ALREADY_EXISTS;
		}
		String expectedServiceName = RPCSystemUtil.getDynamicServiceNameWithSequence(name, sequence);
		ServiceClusterRef expectedServiceClusterRef = system.serviceOf(expectedServiceName);
		if (Objects.nonNull(expectedServiceClusterRef) && expectedServiceClusterRef.size() != 0) {
			return SERVICE_ALREADY_EXISTS;
		}
		ServiceRef creator = null;
		if (msg.isLocation()) {
			creator = RPCSystemUtil.getLocalServiceRef(clusterRef);
		} else {
			ServiceRef leader = clusterRef.leader();
			//如果我不是leader,那么交给leader去管理
			if (leader != ctx.self()) {
				return leader.ask(msg);
			}
			creator = pickupServiceRef(clusterRef);
		}
		return docker.inquire(new ServiceCreateParam(creator, ctx, sequence));
	}

	/**
	 * 创建服务逻辑
	 */
	protected CompletableFuture<Object> createService(ServiceCreateParam param) throws Throwable {
		ServiceRef destination = param.destination;
		ServiceContext context = param.context;
		ServiceRef self = context.self();
		String sequence = param.sequence;
		DynamicServiceCreateMessage createMsg = new DynamicServiceCreateMessage(sequence);
		if (destination == self) {
			@SuppressWarnings("unchecked")
			CompletableFuture<Object> f = (CompletableFuture<Object>) handle(createMsg, context);
			f.thenAccept(e -> createdServices.add(sequence));
			return f;
		}
		return destination.ask(createMsg).thenApply(e -> createdServices.add(sequence));
	}

	/**
	 * 筛选出服务数最小的服务
	 * @author NewYoung
	 * 2017年3月23日下午4:07:38
	 */
	private ServiceRef pickupServiceRef(ServiceClusterRef cluster) {
		ServiceRef min = cluster.one();
		int minValue = min.getOrDefault(SERVICE_BALANCE_KEY, 0);
		for (ServiceRef ref : cluster.iterable()) {
			Integer value = ref.getOrDefault(SERVICE_BALANCE_KEY, 0);
			if (value < minValue) {
				min = ref;
				minValue = value;
			}
		}
		min.attach(SERVICE_BALANCE_KEY, minValue + 1);
		return min;
	}

	private static class ServiceCreateParam {
		public ServiceRef destination;
		public ServiceContext context;
		public String sequence;

		public ServiceCreateParam(ServiceRef destination, ServiceContext context, String sequence) {
			super();
			this.destination = destination;
			this.context = context;
			this.sequence = sequence;
		}
	}

	/**
	 * 负载均衡调整
	 * @author NewYoung
	 * 2017年5月9日下午5:10:10
	 */
	void adjustDynamicServiceBalance(ServiceRef ref, int adjustment) {
		ServiceClusterRef cluster = system.serviceOf(name);
		for (ServiceRef creator : cluster.iterable()) {
			if (ref.isSameAddress(creator)) {
				int oldValue = creator.getOrDefault(SERVICE_BALANCE_KEY, 0);
				int newValue = oldValue + adjustment;
				creator.attach(SERVICE_BALANCE_KEY, newValue);
			}
		}
	}

	/**
	 * 处理动态服务重名问题
	 * @author NewYoung
	 * 2017年5月10日上午10:34:59
	 */
	void checkSameNameServiceIfNescessary(ServiceRef ref) {
		if (system.serviceOf(ref.name()).size() < 2) {
			return;
		}
		ServiceContext context = ServiceContextUtil.context();
		if (!Objects.equals(system.serviceOf(this.name).leader(), context.self())) {
			return;
		}
		logger.info("leader detect dynamic service was duplicate prepar to stop {}", ref);
		ref.ask(new CommandMessage(CommandMessage.CLOSE_COMMAND));
	}

}
