package com.shinemo.mpush.tools.thread.threadpool.fixed;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.shinemo.mpush.tools.thread.NamedThreadFactory;
import com.shinemo.mpush.tools.thread.threadpool.IgnoreRunsPolicy;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPool;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolContext;

/**
 *此线程池启动时即创建固定大小的线程数，不做任何伸缩 
 *
 */
public class FixedThreadPool implements ThreadPool {

	@Override
	public Executor getExecutor(ThreadPoolContext context) {
		String name = context.getName();
		int corePoolSize = context.getCorePoolSize();
		int queueCapacity = context.getQueueCapacity();

		BlockingQueue<Runnable> blockingQueue = null;
		if (queueCapacity == 0) {
			blockingQueue = new SynchronousQueue<Runnable>();
		} else if (queueCapacity < 0) {
			blockingQueue = new LinkedBlockingQueue<Runnable>();
		} else {
			blockingQueue = new LinkedBlockingQueue<Runnable>(queueCapacity);
		}

		final ThreadFactory threadFactory = new NamedThreadFactory(name);

		return new ThreadPoolExecutor(corePoolSize, corePoolSize, 0, TimeUnit.SECONDS, blockingQueue, threadFactory, new IgnoreRunsPolicy(context));
	}

}
