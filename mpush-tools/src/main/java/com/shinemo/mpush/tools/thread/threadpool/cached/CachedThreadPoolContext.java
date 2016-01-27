package com.shinemo.mpush.tools.thread.threadpool.cached;

import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolContext;

public class CachedThreadPoolContext extends ThreadPoolContext{

	public CachedThreadPoolContext(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds) {
		super(name, corePoolSize, maxPoolSize, 0, keepAliveSeconds);
	}
	
	public static CachedThreadPoolContext create(String name, int corePoolSize, int maxPoolSize, int keepAliveSeconds){
		return new CachedThreadPoolContext(name, corePoolSize, maxPoolSize, keepAliveSeconds);
	}

}
