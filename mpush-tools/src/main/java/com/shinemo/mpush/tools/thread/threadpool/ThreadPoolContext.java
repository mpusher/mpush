package com.shinemo.mpush.tools.thread.threadpool;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.thread.ThreadNameSpace;
import com.shinemo.mpush.tools.thread.threadpool.cached.CachedThreadPoolContext;
import com.shinemo.mpush.tools.thread.threadpool.fixed.FixedThreadPoolContext;


public class ThreadPoolContext {
	
	private final String name;//名字
	private final int cores; //最小线程大小
	private final int threads; //最大线程大小
	private final int queues;  // queues > 0,则FIFO队列,
	private final int alive;// 存活时间
	
	public static ThreadPoolContext BOSS_THREAD_POOL = CachedThreadPoolContext.create(ThreadNameSpace.NETTY_BOSS, Constants.MIN_BOSS_POOL_SIZE, Constants.MAX_BOSS_POLL_SIZE, 1000*60*5);
	
	public static ThreadPoolContext WORK_THREAD_POOL = CachedThreadPoolContext.create(ThreadNameSpace.NETTY_WORKER, Constants.MIN_WORK_POOL_SIZE, Constants.MAX_WORK_POOL_SIZE, 1000*60*5);
	
	public static ThreadPoolContext BIZ_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.BIZ, Constants.BIZ_POOL_SIZE);
	
	public static ThreadPoolContext EVENT_BUS_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.EVENT_BUS, Constants.EVENT_BUS_POOL_SIZE);
	
	public ThreadPoolContext(String name, int cores, int threads, int queues, int alive) {
		this.name = name;
		this.cores = cores;
		this.threads = threads;
		this.queues = queues; 
		this.alive = alive;
	}
	
	public String getName() {
		return name;
	}
	public int getCores() {
		return cores;
	}
	public int getThreads() {
		return threads;
	}
	public int getQueues() {
		return queues;
	}
	public int getAlive() {
		return alive;
	}

	@Override
	public String toString() {
		return "ThreadPoolContext [name=" + name + ", cores=" + cores + ", threads=" + threads + ", queues=" + queues + ", alive=" + alive + "]";
	}

	
}
