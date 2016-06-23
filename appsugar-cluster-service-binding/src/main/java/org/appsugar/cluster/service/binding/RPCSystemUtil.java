package org.appsugar.cluster.service.binding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.api.CompletableFutureUtil;
import org.appsugar.cluster.service.api.FutureMessage;
import org.appsugar.cluster.service.api.KeyValue;
import org.appsugar.cluster.service.api.ServiceContext;
import org.appsugar.cluster.service.api.ServiceContextThreadLocal;
import org.appsugar.cluster.service.api.ServiceException;
import org.appsugar.cluster.service.api.ServiceRef;
import org.appsugar.cluster.service.api.Status;
import org.appsugar.cluster.service.binding.annotation.DynamicService;
import org.appsugar.cluster.service.binding.annotation.ExecuteDefault;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnEvent;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.binding.annotation.ExecuteRepeat;
import org.appsugar.cluster.service.binding.annotation.Service;

/**
 * 远程调用帮助类
 * @author NewYoung
 * 2016年6月3日上午7:15:11
 */
public class RPCSystemUtil {

	/**
	 * 获取服务名称
	 */
	public static final String getServiceName(Class<?> interfaceClass) {
		if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException(interfaceClass + "  isn't a interface");
		}
		Service service = interfaceClass.getAnnotation(Service.class);
		if (service == null) {
			throw new ServiceException("interface " + interfaceClass + " did not annotated with Service");
		}
		return service.value();
	}

	/**
	 * 获取初始化调用方法
	 */
	public static final List<MethodInvoker> getDefaultInvoker(Map<Class<?>, ?> serves) {
		return serves.entrySet().stream()
				.flatMap(e -> getDefaultMethod(e.getValue()).stream().map(m -> new MethodInvoker(m, e.getValue())))
				.collect(Collectors.toList());
	}

	/**
	 * 查找该类中注解为ExecuteDefault无参数的方法
	 */
	public static final List<Method> getDefaultMethod(Object target) {
		Class<?> clazz = target.getClass();
		return Arrays.asList(clazz.getMethods()).stream()
				.filter(m -> m.isAnnotationPresent(ExecuteDefault.class) && m.getParameterCount() == 0)
				.collect(Collectors.toList());
	}

	public static final List<RepeatInvoker> getRepeatInvoker(Map<Class<?>, ?> serves) {
		return serves.entrySet().stream()
				.flatMap(e -> RPCSystemUtil.getRepeatMethods(e.getValue()).stream()
						.map(k -> new RepeatInvoker(k.getAnnotation(ExecuteRepeat.class).value(),
								new MethodInvoker(k, e.getValue()))))
				.collect(Collectors.toList());
	}

	/**
	 * 获取该对象所有注解为ExecuteRepeat无参方法
	 */
	public static final List<Method> getRepeatMethods(Object target) {
		Class<?> clazz = target.getClass();
		return Arrays.asList(clazz.getMethods()).stream()
				.filter(m -> m.isAnnotationPresent(ExecuteRepeat.class) && m.getParameterCount() == 0)
				.collect(Collectors.toList());
	}

	/**
	 * 获取事件调用方法
	 */
	public static final Map<String, List<MethodInvoker>> getEventMethodInvoke(Map<Class<?>, ?> serves) {
		return serves.entrySet().stream()
				.flatMap(e -> RPCSystemUtil.getEventMethods(e.getValue()).entrySet().stream()
						.map(k -> new KeyValue<>(k.getKey(),
								k.getValue().stream().map(m -> new MethodInvoker(m, serves.get(e.getKey()))))))
				.flatMap(e -> e.getValue().map(s -> new KeyValue<>(e.getKey(), s))).collect(Collectors
						.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toList())));

	}

	/**
	 * 查找该类中注解为ExecuteOnEvent并且只有一个参数的方法
	 */
	public static final Map<String, List<Method>> getEventMethods(Object target) {
		Class<?> clazz = target.getClass();
		List<KeyValue<String, Method>> defaultMethods = Arrays.asList(clazz.getMethods()).stream()
				.filter(m -> m.isAnnotationPresent(ExecuteOnEvent.class) && m.getParameterCount() == 1)
				.map(e -> new KeyValue<>(e.getAnnotation(ExecuteOnEvent.class).value(), e))
				.collect(Collectors.toList());
		return defaultMethods.stream().collect(
				Collectors.groupingBy(e -> e.getKey(), Collectors.mapping(e -> e.getValue(), Collectors.toList())));
	}

	/**
	 * 获取服务状态帮助
	 */
	public static final Map<String, List<ServiceStatusHelper>> getServiceStatusHelper(Map<Class<?>, ?> serves) {
		return serves.entrySet().stream()
				.flatMap(e -> RPCSystemUtil.getServiceReadyMethods(e.getValue()).entrySet().stream()
						.map(k -> new KeyValue<>(k.getKey(),
								new ServiceStatusHelper(k.getKey(), k.getValue(), e.getValue()))))
				.collect(Collectors.groupingBy(e -> RPCSystemUtil.getServiceName(e.getKey()),
						Collectors.mapping(e -> e.getValue(), Collectors.toList())));
	}

	/**
	 * 获取该类中注解为ExecuteOnServiceReady
	 */
	public static final Map<Class<?>, Method> getServiceReadyMethods(Object target) {
		Class<?> clazz = target.getClass();
		return Arrays.asList(clazz.getMethods()).stream()
				.filter(m -> m.isAnnotationPresent(ExecuteOnServiceReady.class)
						&& m.getParameterTypes()[1].equals(Status.class))
				.map(m -> new KeyValue<>(m.getParameterTypes()[0], m))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/**
	 * 根据接口获取调用map
	 */
	public static final Map<List<String>, MethodInvoker> getClassMethodInvoker(Map<Class<?>, ?> classMap) {
		return classMap.entrySet().stream()
				.flatMap(
						e -> Arrays.asList(e.getKey().getMethods()).stream()
								.map(m -> new KeyValue<>(getNameList(e.getKey(), m),
										new MethodInvoker(m, e.getValue()))))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/**
	 * 获取名称集合 类名+方法名+参数名
	 */
	public static final List<String> getNameList(Class<?> clazz, Method method) {
		List<String> nameList = new ArrayList<>();
		nameList.add(clazz.getName());
		nameList.add(method.getName());
		for (Class<?> c : method.getParameterTypes()) {
			nameList.add(c.getName());
		}
		return nameList;
	}

	/**
	 * 获取动态服务名称
	 */
	public static final String getDynamicServiceName(Class<?> clazz) {
		if (!clazz.isInterface()) {
			throw new ServiceException(clazz + " is not interface");
		}
		DynamicService service = clazz.getAnnotation(DynamicService.class);
		if (service == null) {
			throw new ServiceException("interface " + clazz + " did not annotated with DynamicService");
		}
		return service.value();
	}

	/**
	 * 根据sequence获取动态服务名称
	 */
	public static final String getDynamicServiceNameWithSequence(String name, String sequence) {
		return name + "/" + sequence;
	}

	/**
	 * 确保future通知执行在当前context中
	 */
	public static <T> CompletableFuture<T> wrapContextFuture(CompletableFuture<T> future) {
		ServiceContext context = ServiceContextThreadLocal.context();
		if (context == null || future.isDone() || future.isCancelled()) {
			return future;
		}
		CompletableFuture<T> notifyFuture = new CompletableFuture<>();
		future.whenComplete((r, e) -> {
			ServiceContext ctx = ServiceContextThreadLocal.context();
			if (ctx == context) {
				CompletableFutureUtil.completeNormalOrThrowable(notifyFuture, r, e);
			} else {
				context.self()
						.tell(new FutureMessage<T>(r, e,
								(r1, e1) -> CompletableFutureUtil.completeNormalOrThrowable(notifyFuture, r1, e1)),
								ctx == null ? ServiceRef.NO_SENDER : ctx.sender());
			}
		});
		return notifyFuture;
	}

}