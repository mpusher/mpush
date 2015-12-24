package com.shinemo.mpush.core.thread;

import com.shinemo.mpush.api.Constants;


public class ThreadPoolUtil {

	private static final ThreadPoolManager threadPoolManager = new ThreadPoolManager(Constants.MIN_POOL_SIZE, Constants.MAX_POOL_SIZE,
			Constants.THREAD_QUEUE_SIZE);

	public static ThreadPoolManager getThreadPoolManager() {
		return threadPoolManager;
	}

}
