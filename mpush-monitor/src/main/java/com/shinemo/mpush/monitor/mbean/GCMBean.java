package com.shinemo.mpush.monitor.mbean;

public interface GCMBean {

	public long yongGcCollectionCount();
	
	public long yongGcCollectionTime();
	
	public long fullGcCollectionCount();
	
	public long fullGcCollectionTime();
	
	public long spanYongGcCollectionCount();
	
	public long spanYongGcCollectionTime();
	
	public long spanFullGcCollectionCount();
	
	public long spanFullGcCollectionTime();
	
}
