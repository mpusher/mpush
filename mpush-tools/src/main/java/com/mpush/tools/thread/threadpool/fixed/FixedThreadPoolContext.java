package com.mpush.tools.thread.threadpool.fixed;

import com.mpush.tools.thread.threadpool.ThreadPoolContext;

public class FixedThreadPoolContext extends ThreadPoolContext {

	public FixedThreadPoolContext(String name, int threads,int queueCapacity) {
		super(name, threads, 0, queueCapacity, 0);
	}
	
	public FixedThreadPoolContext(String name, int threads) {
		super(name, threads, 0, -1, 0);
	}
	
	public static FixedThreadPoolContext create(String name,int threads){
		return new FixedThreadPoolContext(name, threads);
	}
	
	public static FixedThreadPoolContext create(String name,int threads,int queueCapacity){
		return new FixedThreadPoolContext(name, threads,queueCapacity);
	}

}
