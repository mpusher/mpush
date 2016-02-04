package com.shinemo.mpush.tools.thread.threadpool;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.thread.ThreadNameSpace;
import com.shinemo.mpush.tools.thread.threadpool.cached.CachedThreadPoolContext;
import com.shinemo.mpush.tools.thread.threadpool.fixed.FixedThreadPoolContext;


public class ThreadPoolContext {
	
	private final String name;//名字
	private final int corePoolSize; //最小线程大小
	private final int maxPoolSize; //最大线程大小
	private final int queueCapacity;  // 允许缓冲在队列中的任务数 (0:不缓冲、负数：无限大、正数：缓冲的任务数)
	private final int keepAliveSeconds;// 存活时间
	
	public static ThreadPoolContext BOSS_THREAD_POOL = CachedThreadPoolContext.create(ThreadNameSpace.NETTY_BOSS, Constants.MIN_BOSS_POOL_SIZE, Constants.MAX_BOSS_POOL_SIZE, 60*5,Constants.BOSS_THREAD_QUEUE_SIZE);
	
	public static ThreadPoolContext WORK_THREAD_POOL = CachedThreadPoolContext.create(ThreadNameSpace.NETTY_WORKER, Constants.MIN_WORK_POOL_SIZE, Constants.MAX_WORK_POOL_SIZE, 60*5,Constants.WORK_THREAD_QUEUE_SIZE);
	
	public static ThreadPoolContext BIZ_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.BIZ, Constants.BIZ_POOL_SIZE);
	
	public static ThreadPoolContext EVENT_BUS_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.EVENT_BUS, Constants.EVENT_BUS_POOL_SIZE);
	
	public static ThreadPoolContext REDIS_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.REDIS, Constants.REDIS_POOL_SIZE,Constants.REDIS_THREAD_QUEUE_SIZE);
	
	public static ThreadPoolContext ZK_THREAD_POOL = FixedThreadPoolContext.create(ThreadNameSpace.ZK, Constants.ZK_POOL_SIZE,Constants.ZK_THREAD_QUEUE_SIZE);
	
	public ThreadPoolContext(String name, int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds) {
		this.name = name;
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.queueCapacity = queueCapacity;
		this.keepAliveSeconds = keepAliveSeconds;
	}

	public String getName() {
		return name;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	@Override
	public String toString() {
		return "ThreadPoolContext [name=" + name + ", corePoolSize=" + corePoolSize + ", maxPoolSize=" + maxPoolSize + ", queueCapacity=" + queueCapacity + ", keepAliveSeconds=" + keepAliveSeconds
				+ "]";
	}
	
}
