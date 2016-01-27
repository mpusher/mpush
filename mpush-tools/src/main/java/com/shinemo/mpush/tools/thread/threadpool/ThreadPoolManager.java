package com.shinemo.mpush.tools.thread.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


import com.shinemo.mpush.tools.spi.ServiceContainer;

public class ThreadPoolManager {

	private static final Map<String, Executor> poolCache = new HashMap<String, Executor>();

	private static ThreadPool cachedThreadPool = ServiceContainer.getInstance(ThreadPool.class, "cachedThreadPool");
	
	private static ThreadPool fixedThreadPool = ServiceContainer.getInstance(ThreadPool.class, "fixedThreadPool");
	
	public static Executor bossExecutor = cachedThreadPool.getExecutor(ThreadPoolContext.BOSS_THREAD_POOL);
	public static Executor workExecutor = cachedThreadPool.getExecutor(ThreadPoolContext.WORK_THREAD_POOL);
	public static Executor bizExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.BIZ_THREAD_POOL);
	public static Executor eventBusExecutor = fixedThreadPool.getExecutor(ThreadPoolContext.EVENT_BUS_THREAD_POOL);
	
	static{
		poolCache.put(ThreadPoolContext.BOSS_THREAD_POOL.getName(), bossExecutor);
		poolCache.put(ThreadPoolContext.WORK_THREAD_POOL.getName(), workExecutor);
		poolCache.put(ThreadPoolContext.BIZ_THREAD_POOL.getName(), bizExecutor);
		poolCache.put(ThreadPoolContext.EVENT_BUS_THREAD_POOL.getName(), eventBusExecutor);
	}
	
	public static final Map<String, Executor> getPool(){
		return poolCache;
	}

	

}
