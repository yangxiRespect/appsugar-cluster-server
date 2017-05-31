package org.appsugar.cluster.service.binding.spring;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.appsugar.cluster.service.binding.ServiceDocker;
import org.appsugar.cluster.service.util.CompletableFutureUtil;
import org.appsugar.commons.index.Index;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import function.ThrowableSupplier;

/**
 * 基础service  
 */
public abstract class AbstractFacade {
	protected AsyncExecutor executor;

	@Autowired
	public void setExecutor(AsyncExecutor executor) {
		this.executor = executor;
	}

	/**
	 * 异步执行任务
	 */
	protected <T> CompletableFuture<T> async(ThrowableSupplier<T> supplier) {
		return executor.execute(supplier);
	}

	/**
	 * 只读事物异步执行
	 */
	protected <T> CompletableFuture<T> asyncReadonly(ThrowableSupplier<T> supplier) {
		return executor.executeInTransaction(supplier, true);
	}

	/**
	 * 在事物中异步执行
	 */
	protected <T> CompletableFuture<T> asyncTransactional(ThrowableSupplier<T> supplier) {
		return executor.executeInTransaction(supplier, false);
	}

	/**
	 *构建缓存 
	 */
	protected <K, V> Cache<K, V> buildCache() {
		return CacheBuilder.newBuilder().build();
	}

	/**
	 *构建缓存 
	 */
	protected <K, V> Cache<K, V> buildCache(long duration, TimeUnit unit) {
		return CacheBuilder.newBuilder().expireAfterAccess(duration, unit).build();
	}

	/**
	 * 完成future
	 */
	protected <U> CompletableFuture<U> complete(U result) {
		return CompletableFuture.completedFuture(result);
	}

	/**
	 * 完成future
	 */
	protected <T> void complete(CompletableFuture<T> future, T result, Throwable ex) {
		CompletableFutureUtil.completeNormalOrThrowable(future, result, ex);
	}

	/**
	 * 根据缓存,不存在key的缓存与docker获取具体对象
	 * 如果没有查找到对应数据,那么标记到noneKey缓存中.优化下次查询
	 * @param key 查询参数
	 * @param index 对应索引
	 * @param noneKey 无key存在索引
	 * @param docker 数据请求工具  
	 */
	protected <T, P> CompletableFuture<T> resultOf(P key, Index<P, T> index, Cache<Object, Boolean> noneKey,
			ServiceDocker<T, P> docker) {
		return resultOf(key, index, noneKey, docker, null);
	}

	/**
	 * 根据缓存,不存在key的缓存与docker获取具体对象
	 * 如果没有查找到对应数据,那么标记到noneKey缓存中.优化下次查询
	 * @param key 查询参数
	 * @param index 对应索引
	 * @param noneKey 无key存在索引（可以为空）
	 * @param docker 数据请求工具 
	 * @param postProcess 数据查询后处理器（可以为空）
	 */
	protected <T, P> CompletableFuture<T> resultOf(P key, Index<P, T> index, Cache<Object, Boolean> noneKey,
			ServiceDocker<T, P> docker, Consumer<T> postProcess) {
		if (Objects.nonNull(noneKey) && Objects.nonNull(noneKey.getIfPresent(key))) {
			return complete(null);
		}
		T result = index.get(key);
		if (Objects.nonNull(result)) {
			return complete(result);
		}
		CompletableFuture<T> future = docker.inquire(key);
		if (Objects.isNull(postProcess) && Objects.isNull(noneKey)) {
			return future;
		}
		future.thenAccept(e -> {
			if (Objects.nonNull(postProcess)) {
				postProcess.accept(e);
			}
			if (Objects.isNull(e) && Objects.nonNull(noneKey)) {
				noneKey.put(key, Boolean.TRUE);
			}
		});
		return future;
	}

	/**
	 * 根据缓存,不存在key的缓存与docker获取具体对象
	 * 如果没有查找到对应数据,那么标记到noneKey缓存中.优化下次查询
	 * @param key 查询参数
	 * @param index 对应索引
	 * @param noneKey 无key存在索引（可以为空）
	 * @param docker 数据请求工具 
	 * @param postProcess 数据查询后处理器（可以为空）
	 * @param fallbackProcess 失败处理器 (可以为空)
	 */
	protected <T, P> CompletableFuture<T> resultOf(P key, Index<P, T> index, Cache<Object, Boolean> noneKey,
			ServiceDocker<T, P> docker, Consumer<T> postProcess, Consumer<Throwable> fallbackProcess) {
		if (noneKey != null && noneKey.getIfPresent(key) == Boolean.TRUE) {
			return complete(null);
		}
		T result = index.get(key);
		if (result != null) {
			return complete(result);
		}
		CompletableFuture<T> future = docker.inquire(key);
		//如果没有 后置处理器,没有未存在缓存,没有失败处理器.  那么直接返回
		if (Objects.isNull(postProcess) && Objects.isNull(noneKey) && Objects.isNull(fallbackProcess)) {
			return future;
		}
		future.whenComplete((r, e) -> {
			//发生异常,调用失败处理
			if (Objects.nonNull(e) && Objects.nonNull(fallbackProcess)) {
				fallbackProcess.accept(e);
				return;
			}
			//调用成功处理器
			if (Objects.nonNull(postProcess)) {
				postProcess.accept(r);
			}
			//如果没有查询到值把对应key加入到未存在缓存中
			if (Objects.isNull(r) && noneKey != null) {
				noneKey.put(key, Boolean.TRUE);
			}
		});
		return future;
	}

}