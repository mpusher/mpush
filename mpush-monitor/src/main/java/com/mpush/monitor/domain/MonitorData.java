package com.mpush.monitor.domain;

import java.util.Map;

public class MonitorData {
	
	private Long timestamp;

	private Map<String, Object> memoryMap;

	private Map<String, Object> gcMap;

	private Map<String, Object> threadMap;
	
	private Map<String,Object> threadPoolMap;
	
	private Map<String,Object> infoMap;

	public MonitorData() {
		this.timestamp = System.currentTimeMillis();
	}

	public Map<String, Object> getMemoryMap() {
		return memoryMap;
	}

	public void setMemoryMap(Map<String, Object> memoryMap) {
		this.memoryMap = memoryMap;
	}

	public Map<String, Object> getGcMap() {
		return gcMap;
	}

	public void setGcMap(Map<String, Object> gcMap) {
		this.gcMap = gcMap;
	}

	public Map<String, Object> getThreadMap() {
		return threadMap;
	}

	public void setThreadMap(Map<String, Object> threadMap) {
		this.threadMap = threadMap;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Map<String, Object> getInfoMap() {
		return infoMap;
	}

	public void setInfoMap(Map<String, Object> infoMap) {
		this.infoMap = infoMap;
	}

	public Map<String, Object> getThreadPoolMap() {
		return threadPoolMap;
	}

	public void setThreadPoolMap(Map<String, Object> threadPoolMap) {
		this.threadPoolMap = threadPoolMap;
	}
	
}
