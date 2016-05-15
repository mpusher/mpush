package com.mpush.tools.thread.threadpool.cached;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mpush.tools.thread.NamedThreadFactory;
import com.mpush.tools.thread.threadpool.IgnoreRunsPolicy;
import com.mpush.tools.thread.threadpool.ThreadPoolContext;
import com.mpush.tools.thread.threadpool.ThreadPool;

/**
 * 此线程池可伸缩，线程空闲一定时间后回收，新请求重新创建线程
 *
 */
public class CachedThreadPool implements ThreadPool {

	@Override
	public Executor getExecutor(ThreadPoolContext context) {

		String name = context.getName();
		int corePoolSize = context.getCorePoolSize();
		int maxPoolSize = context.getMaxPoolSize();
		int queueCapacity = context.getQueueCapacity();
		int keepAliveSeconds = context.getKeepAliveSeconds();

		final ThreadFactory threadFactory = new NamedThreadFactory(name);
		
		BlockingQueue<Runnable> blockingQueue = null;
		if(queueCapacity == 0){
			blockingQueue = new SynchronousQueue<Runnable>();
		}else if(queueCapacity<0){
			blockingQueue = new LinkedBlockingQueue<Runnable>();
		}else{
			blockingQueue = new LinkedBlockingQueue<Runnable>(queueCapacity);
		}
		
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, blockingQueue, threadFactory, new IgnoreRunsPolicy(context));

	}
	
}
