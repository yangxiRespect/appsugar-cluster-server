package org.appsugar.cluster.service.binding;

/**
 * 重复方法调用
 * @author NewYoung
 * 2016年6月3日上午3:19:28
 */
public class RepeatInvoker {

	private long interval;
	private long lastExecuteTime;
	private MethodInvoker invoker;

	public RepeatInvoker(long interval, MethodInvoker invoker) {
		super();
		this.interval = interval;
		this.invoker = invoker;
	}

	/**
	 * 尝试调用(如果时间间隔条件符合) 
	 */
	public void tryInvoke(long currentTime) throws Throwable {
		if (lastExecuteTime == 0 || currentTime >= (lastExecuteTime + interval)) {
			try {
				invoker.invoke(null);
			} finally {
				lastExecuteTime = currentTime;
			}
		}
	}

	public long getInterval() {
		return interval;
	}

	public long getLastExecuteTime() {
		return lastExecuteTime;
	}

	public MethodInvoker getInvoker() {
		return invoker;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RepeatInvoker [interval=").append(interval).append(", lastExecuteTime=").append(lastExecuteTime)
				.append(", invoker=").append(invoker).append("]");
		return builder.toString();
	}

}