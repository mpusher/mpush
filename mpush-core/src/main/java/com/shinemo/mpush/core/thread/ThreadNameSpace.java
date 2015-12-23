package com.shinemo.mpush.core.thread;

public class ThreadNameSpace {
	
	/**
	 * netty boss 线程
	 */
	public static final String NETTY_BOSS = "mg-boss";
	
	/**
	 * netty worker 线程
	 */
	public static final String NETTY_WORKER = "mg-worker"; 
	
	/**
	 * connection 定期检测线程
	 */
	public static final String NETTY_TIMER = "mg-timer";

}
