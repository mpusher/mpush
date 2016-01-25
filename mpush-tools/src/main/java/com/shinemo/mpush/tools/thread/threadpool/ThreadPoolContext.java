package com.shinemo.mpush.tools.thread.threadpool;

public class ThreadPoolContext {
	
	private final String name;
	private final int cores;
	private final int threads;
	private final int queues;
	private final int alive;
	
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
