package org.appsugar.cluster.service.binding;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.api.KeyValue;
import org.appsugar.cluster.service.api.Status;
import org.appsugar.cluster.service.binding.annotation.ExecuteDefault;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnEvent;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.binding.annotation.ExecuteRepeat;
import org.appsugar.cluster.service.binding.annotation.Service;

/**TODO 支持对象父类里的方法
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
			throw new RuntimeException("interface " + interfaceClass + " did not annotated with Service");
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
								new ServiceStatusHelper(e.getKey(), k.getValue(), e.getValue()))))
				.collect(Collectors.groupingBy(e -> RPCSystemUtil.getServiceName(e.getKey()),
						Collectors.mapping(e -> e.getValue(), Collectors.toList())));
	}

	/**
	 * 获取该类中注解为ExecuteOnServiceReady并且无参的方法
	 */
	public static final Map<Class<?>, Method> getServiceReadyMethods(Object target) {
		Class<?> clazz = target.getClass();
		return Arrays.asList(clazz.getMethods()).stream()
				.filter(m -> m.isAnnotationPresent(ExecuteOnServiceReady.class) && Arrays.equals(m.getParameterTypes(),
						new Class[] { m.getAnnotation(ExecuteOnServiceReady.class).value(), Status.class }))
				.map(m -> new KeyValue<>(m.getAnnotation(ExecuteOnServiceReady.class).value(), m))
				.collect(Collectors.toList()).stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	/**
	 * 获取class调用帮助
	 */
	public static final Map<Integer, ClassMethodHelper> getClassMethodHelper(Map<Class<?>, ?> serves) {
		return serves.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getName().hashCode(),
				e -> new ClassMethodHelper(e.getKey(), e.getValue())));
	}

	/**
	 * 获取该接口所有方法,参数名,方法名的hashcode集合为key
	 */
	public static final Map<List<Integer>, Method> getClassMethod(Class<?> interfaceClass) {
		if (!interfaceClass.isInterface()) {
			throw new RuntimeException(interfaceClass + " isn't a Interface");
		}
		Map<List<Integer>, Method> result = Arrays.asList(interfaceClass.getMethods()).stream()
				.collect(Collectors.toMap(m -> {
					List<Integer> key = Arrays.asList(m.getParameterTypes()).stream()
							.collect(Collectors.mapping(c -> c.getName().hashCode(), Collectors.toList()));
					key.add(m.getName().hashCode());
					return key;
				}, m -> m));
		Arrays.asList(interfaceClass.getInterfaces()).stream().forEach(c -> result.putAll(getClassMethod(c)));
		return result;
	}

}