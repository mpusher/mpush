package com.mpush.tools.thread.threadpool.cached;

import com.mpush.tools.thread.threadpool.ThreadPoolContext;

public class CachedThreadPoolContext extends ThreadPoolContext {

	public CachedThreadPoolContext(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds) {
		this(name, corePoolSize, maxPoolSize, keepAliveSeconds, 0);
	}
	
	public CachedThreadPoolContext(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds,int queueCapacity) {
		super(name, corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
	}
	
	public static CachedThreadPoolContext create(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds){
		return new CachedThreadPoolContext(name, corePoolSize, maxPoolSize, keepAliveSeconds);
	}
	
	public static CachedThreadPoolContext create(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds,int queueCapacity){
		return new CachedThreadPoolContext(name, corePoolSize, maxPoolSize, keepAliveSeconds,queueCapacity);
	}

}
