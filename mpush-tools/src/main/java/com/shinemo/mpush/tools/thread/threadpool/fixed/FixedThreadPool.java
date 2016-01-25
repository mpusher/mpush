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
		int threads = context.getThreads();
		int queues = context.getQueues();

		BlockingQueue<Runnable> blockingQueue = null;
		if (queues == 0) {
			blockingQueue = new SynchronousQueue<Runnable>();
		} else if (queues < 0) {
			blockingQueue = new LinkedBlockingQueue<Runnable>();
		} else {
			blockingQueue = new LinkedBlockingQueue<Runnable>(queues);
		}

		final ThreadFactory threadFactory = new NamedThreadFactory(name);

		return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, blockingQueue, threadFactory, new IgnoreRunsPolicy(context));
	}

}
