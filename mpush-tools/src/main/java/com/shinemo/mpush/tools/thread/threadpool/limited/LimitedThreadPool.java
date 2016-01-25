package com.shinemo.mpush.tools.thread.threadpool.limited;

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
 * 此线程池一直增长，直到上限，增长后不收缩。
 *
 */
public class LimitedThreadPool implements ThreadPool {

	@Override
	public Executor getExecutor(ThreadPoolContext context) {

		String name = context.getName();
		int cores = context.getCores();
		int threads = context.getThreads();
		int queues = context.getQueues();

		final ThreadFactory threadFactory = new NamedThreadFactory(name);

		BlockingQueue<Runnable> blockingQueue = null;
		if (queues == 0) {
			blockingQueue = new SynchronousQueue<Runnable>();
		} else if (queues < 0) {
			blockingQueue = new LinkedBlockingQueue<Runnable>();
		} else {
			blockingQueue = new LinkedBlockingQueue<Runnable>(queues);
		}

		return new ThreadPoolExecutor(cores, threads, Long.MAX_VALUE, TimeUnit.MILLISECONDS, blockingQueue, threadFactory, new IgnoreRunsPolicy(context));

	}

}
