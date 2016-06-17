package org.appsugar.cluster.service.binding;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.appsugar.cluster.service.api.Status;
import org.appsugar.cluster.service.binding.annotation.ExecuteDefault;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnEvent;
import org.appsugar.cluster.service.binding.annotation.ExecuteOnServiceReady;
import org.appsugar.cluster.service.binding.annotation.ExecuteRepeat;
import org.appsugar.cluster.service.binding.annotation.Service;
import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * 
 * @author NewYoung
 * 2016年6月3日上午7:52:17
 */
public class RPCSystemUtilTest extends TestCase {

	@Test
	public void testGetServiceName() {
		String serviceName = RPCSystemUtil.getServiceName(MyService.class);
		Assert.assertEquals(MyService.serviceName, serviceName);
	}

	@Test
	public void testGetDefaultMethod() {
		List<Method> methodList = RPCSystemUtil.getDefaultMethod(new MyDefaultClass());
		Assert.assertEquals(2, methodList.size());
	}

	@Test
	public void testGetRepeatMethods() {
		List<Method> methodList = RPCSystemUtil.getRepeatMethods(new MyDefaultClass());
		Assert.assertEquals(1, methodList.size());
	}

	@Test
	public void testGetEventMethods() {
		Map<String, List<Method>> eventMethodMap = RPCSystemUtil.getEventMethods(new MyDefaultClass());
		Assert.assertEquals(1, eventMethodMap.size());
	}

	@Test
	public void testGetServiceReadyMethods() {
		Map<Class<?>, Method> serviceReadyMethodMap = RPCSystemUtil.getServiceReadyMethods(new MyDefaultClass());
		Assert.assertEquals(1, serviceReadyMethodMap.size());
	}

}

@Service(MyService.serviceName)
interface MyService {
	public static final String serviceName = "myService";

	public void sayHello(String hello);

	public void sayHello(String hello, String who);

	public void sayHello(Integer i);

	public void sayHello(int i);

}

class MyDefaultClass {
	@ExecuteDefault
	public void init() {

	}

	@ExecuteRepeat(2000)
	@ExecuteDefault
	public void init1() {

	}

	@ExecuteOnEvent("playStatus")
	public void onPlayOnline(Object o) {
		//do nothing
		//event execute must have only one argument
	}

	@ExecuteOnServiceReady
	public void onProductServiceReady(MyService s, Status status) {

	}
}