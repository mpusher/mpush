package com.shinemo.mpush.monitor.quota;


public interface ThreadQuota {

	public int daemonThreadCount();
	
	public int threadCount();
	
	public long totalStartedThreadCount();
	
	public int deadLockedThreadCount();
	
}
