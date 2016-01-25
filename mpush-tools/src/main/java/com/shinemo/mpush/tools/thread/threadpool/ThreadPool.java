package com.shinemo.mpush.tools.thread.threadpool;

import java.util.concurrent.Executor;

import com.shinemo.mpush.tools.spi.SPI;

@SPI("cachedThreadPool")
public interface ThreadPool {

	public Executor getExecutor(ThreadPoolContext context);
	
}
