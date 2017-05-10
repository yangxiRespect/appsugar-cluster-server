package function;

/**
 * 可抛异常Runnable
 * @author NewYoung
 * 2017年5月2日下午3:32:43
 */
@FunctionalInterface
public interface ThrowableRunnable {

	public void run() throws Throwable;
}
