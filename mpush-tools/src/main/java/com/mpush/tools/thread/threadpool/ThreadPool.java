package com.mpush.tools.thread.threadpool;

import java.util.concurrent.Executor;

import com.mpush.tools.spi.SPI;

@SPI("cachedThreadPool")
public interface ThreadPool {

	public Executor getExecutor(ThreadPoolContext context);
	
}
