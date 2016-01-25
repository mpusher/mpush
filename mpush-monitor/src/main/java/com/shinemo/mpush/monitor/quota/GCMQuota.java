package com.shinemo.mpush.monitor.quota;

public interface GCMQuota {

	public long yongGcCollectionCount();
	
	public long yongGcCollectionTime();
	
	public long fullGcCollectionCount();
	
	public long fullGcCollectionTime();
	
	public long spanYongGcCollectionCount();
	
	public long spanYongGcCollectionTime();
	
	public long spanFullGcCollectionCount();
	
	public long spanFullGcCollectionTime();
	
}
