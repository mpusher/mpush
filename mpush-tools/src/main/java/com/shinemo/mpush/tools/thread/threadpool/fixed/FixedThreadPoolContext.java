package com.shinemo.mpush.tools.thread.threadpool.fixed;

import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolContext;

public class FixedThreadPoolContext extends ThreadPoolContext{

	public FixedThreadPoolContext(String name, int threads) {
		super(name, 0, threads, -1, 0);
	}
	
	public static FixedThreadPoolContext create(String name,int threads){
		return new FixedThreadPoolContext(name, threads);
	}

}
