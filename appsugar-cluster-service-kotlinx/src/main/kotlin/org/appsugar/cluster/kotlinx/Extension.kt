package org.appsugar.cluster.kotlinx

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.future.future
import org.appsugar.cluster.service.api.ServiceContext
import org.appsugar.cluster.service.api.ServiceRef
import org.appsugar.cluster.service.domain.FutureMessage
import org.appsugar.cluster.service.util.ServiceContextUtil
import java.util.concurrent.*

val DISPATCHER_KEY="kotlin_dispatcher"
fun <T> akkaFuture(
        block: suspend CoroutineScope.() -> T
): CompletableFuture<T> {
    val context = ServiceContextUtil.context() ?: return future(block=block)
    val dispatcher = context.getAttribute<CoroutineDispatcher>(DISPATCHER_KEY)?:context.addAttribute(DISPATCHER_KEY,AkkaContextExecutor(context).asCoroutineDispatcher()).let { context.getAttribute<CoroutineDispatcher>(DISPATCHER_KEY) }
    return future(context = dispatcher,block=block)
}

class AkkaContextExecutor(private val context: ServiceContext): AbstractExecutorService(), ScheduledExecutorService {

    override fun execute(command: Runnable) = when(ServiceContextUtil.context()){
        //如果在当前环境中,那么直接执行
        context -> command.run()
        //反之切换到上下文中并执行
        else -> context.self().tell(FutureMessage(null,null,{_,_:Throwable?->command.run()}),ServiceContextUtil.context()?.sender()?:ServiceRef.NO_SENDER)
    }
    override fun schedule(command: Runnable?, delay: Long, unit: TimeUnit?): ScheduledFuture<*> {
        TODO("not implemented")
    }

    override fun isTerminated(): Boolean {
        TODO("not implemented")
    }

    override fun shutdown() {
        TODO("not implemented")
    }

    override fun shutdownNow(): MutableList<Runnable> {
        TODO("not implemented")
    }

    override fun isShutdown(): Boolean {
        TODO("not implemented")
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        TODO("not implemented")
    }


    override fun <V : Any?> schedule(callable: Callable<V>?, delay: Long, unit: TimeUnit?): ScheduledFuture<V> {
        TODO("not implemented")
    }

    override fun scheduleAtFixedRate(command: Runnable?, initialDelay: Long, period: Long, unit: TimeUnit?): ScheduledFuture<*> {
        TODO("not implemented")
    }

    override fun scheduleWithFixedDelay(command: Runnable?, initialDelay: Long, delay: Long, unit: TimeUnit?): ScheduledFuture<*> {
        TODO("not implemented")
    }

}