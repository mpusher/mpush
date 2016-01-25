package com.shinemo.mpush.monitor.mbean;


public interface ThreadMBean {

	public int daemonThreadCount();
	
	public int threadCount();
	
	public long totalStartedThreadCount();
	
	public int deadLockedThreadCount();
	
}
