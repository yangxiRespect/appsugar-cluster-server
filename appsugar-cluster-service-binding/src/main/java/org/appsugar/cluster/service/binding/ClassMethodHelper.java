package org.appsugar.cluster.service.binding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.appsugar.cluster.service.akka.system.AskPatternException;

/**
 * 接口方法调用帮助
 * @author NewYoung
 * 2016年6月3日上午3:01:42
 */
public class ClassMethodHelper {

	private Map<List<Integer>, MethodInvoker> methodMap;

	public ClassMethodHelper(Class<?> interfaceClass, Object target) {
		super();
		methodMap = RPCSystemUtil.getClassMethod(interfaceClass).entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> new MethodInvoker(e.getValue(), target)));
	}

	/**
	 *根据方法名hashcode 和参数hashcode集合 去查询对应方法调用
	 */
	public Object searchAndInvoke(List<Integer> paramTypeList, Object[] paramArray) throws Exception {
		MethodInvoker invoker = methodMap.get(paramTypeList);
		if (invoker == null) {
			//优点:加快速度直接以异常方式返回.  缺点:必须依赖akka实现
			return new AskPatternException("method  not found " + paramTypeList.toString());
		}
		return invoker.invoke(paramArray);
	}

}