package com.shinemo.mpush.tools.thread.threadpool;

public class ThreadPoolContext {
	
	private final String name;//名字
	private final int cores; //最小线程大小
	private final int threads; //最大线程大小
	private final int queues;  // queues > 0,则FIFO队列,
	private final int alive;// 存活时间
	
	public ThreadPoolContext(String name, int cores, int threads, int queues, int alive) {
		this.name = name;
		this.cores = cores;
		this.threads = threads;
		this.queues = queues; 
		this.alive = alive;
	}
	
	public String getName() {
		return name;
	}
	public int getCores() {
		return cores;
	}
	public int getThreads() {
		return threads;
	}
	public int getQueues() {
		return queues;
	}
	public int getAlive() {
		return alive;
	}

	
	
}
