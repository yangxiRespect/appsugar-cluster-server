package org.appsugar.cluster.service.api;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * 服务引用,用于给对应服务发送消息
 * @author NewYoung
 * 2016年5月23日下午2:16:32
 */
public interface ServiceRef extends Attachable {

	public static final ServiceRef NO_SENDER = null;

	/**
	 * {@link ServiceRef#ask(Object, long)} 
	 */
	<T> T ask(Object msg);

	/**
	 * 同步请求服务并获取返回结果
	 * @param msg 发送给服务的消息
	 * @param timeout 服务响应超时时间
	 */
	<T> T ask(Object msg, long timeout);

	/**
	 * {@link ServiceRef#ask(Object, Consumer, Consumer, long)} 
	 */
	<T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error);

	/**
	 * 异步请求服务
	 * @param msg 发送给服务器的消息
	 * @param success 服务器调用成功后返回的消息
	 * @param error 服务器调用失败后返回的错误
	 * @param timeout 超时时间
	 */
	<T> void ask(Object msg, Consumer<T> success, Consumer<Throwable> error, long timeout);

	/**
	 * 给服务器发消息,线程不会阻塞.
	 * @param msg 发送给服务器的消息
	 * @param sender 指定发送者
	 * @return 
	 */
	void tell(Object msg, ServiceRef sender);

	/**
	 * 当前服务是否同一jvm中的服务
	 */
	boolean hasLocalScope();

	/**
	 * 服务名称
	 */
	String name();

	/**
	 * 服务描述
	 */
	String description();

	/**
	 * 服务主机
	 */
	Optional<String> host();

	/**
	 * 服务端口
	 */
	String hostPort();

}