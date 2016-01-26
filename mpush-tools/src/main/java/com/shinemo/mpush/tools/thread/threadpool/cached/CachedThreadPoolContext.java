package com.shinemo.mpush.tools.thread.threadpool.cached;

import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolContext;

public class CachedThreadPoolContext extends ThreadPoolContext{

	public CachedThreadPoolContext(String name, int cores, int threads, int alive) {
		super(name, cores, threads, 0, alive);
	}
	
	public static CachedThreadPoolContext create(String name, int cores, int threads, int alive){
		return new CachedThreadPoolContext(name, cores, threads, alive);
	}

}
