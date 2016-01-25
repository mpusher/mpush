package com.shinemo.mpush.monitor.mbean.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;

import com.google.common.collect.Maps;
import com.shinemo.mpush.monitor.mbean.BaseMBean;
import com.shinemo.mpush.monitor.mbean.GCMBean;

public class JVMGC extends BaseMBean implements GCMBean {

	public static final JVMGC instance = new JVMGC();

	private GarbageCollectorMXBean fullGc;
	private GarbageCollectorMXBean yongGc;

	private long lastYoungGcCollectionCount = -1;
	private long lastYoungGcCollectionTime = -1;
	private long lastFullGcCollectionCount = -1;
	private long lastFullGcCollectionTime = -1;

	
	private JVMGC() {
		for (GarbageCollectorMXBean item : ManagementFactory.getGarbageCollectorMXBeans()) {
			String name = item.getName();
			if (contain(name, youngGcName)) {
				yongGc = item;
			} else if (contain(name,fullGcName)) {
				fullGc = item;
			}
		}

	}

	@Override
	public long yongGcCollectionCount() {
		if (yongGc == null) {
			return 0;
		}
		return yongGc.getCollectionCount();
	}

	@Override
	public long yongGcCollectionTime() {
		if (yongGc == null) {
			return 0;
		}
		return yongGc.getCollectionTime();
	}

	@Override
	public long fullGcCollectionCount() {
		if (fullGc == null) {
			return 0;
		}
		return fullGc.getCollectionCount();
	}

	@Override
	public long fullGcCollectionTime() {
		if (fullGc == null) {
			return 0;
		}
		return fullGc.getCollectionTime();
	}

	@Override
	public long spanYongGcCollectionCount() {

		long current = yongGcCollectionCount();
		if (lastYoungGcCollectionCount == -1) {
			lastYoungGcCollectionCount = current;
			return 0;
		} else {
			long result = current - lastYoungGcCollectionCount;
			lastYoungGcCollectionCount = current;
			return result;
		}

	}

	@Override
	public long spanYongGcCollectionTime() {
		long current = yongGcCollectionTime();
		if (lastYoungGcCollectionTime == -1) {
			lastYoungGcCollectionTime = current;
			return 0;
		} else {
			long result = current - lastYoungGcCollectionTime;
			lastYoungGcCollectionTime = current;
			return result;
		}
	}

	@Override
	public long spanFullGcCollectionCount() {
		long current = fullGcCollectionCount();
		if (lastFullGcCollectionCount == -1) {
			lastFullGcCollectionCount = current;
			return 0;
		} else {
			long result = current - lastFullGcCollectionCount;
			lastFullGcCollectionCount = current;
			return result;
		}
	}

	@Override
	public long spanFullGcCollectionTime() {
		long current = fullGcCollectionTime();
		if (lastFullGcCollectionTime == -1) {
			lastFullGcCollectionTime = current;
			return 0;
		} else {
			long result = current - lastFullGcCollectionTime;
			lastFullGcCollectionTime = current;
			return result;
		}
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("yongGcCollectionCount", yongGcCollectionCount());
		map.put("yongGcCollectionTime", yongGcCollectionTime());
		map.put("fullGcCollectionCount", fullGcCollectionCount());
		map.put("fullGcCollectionTime", fullGcCollectionTime());
		map.put("spanYongGcCollectionCount", spanYongGcCollectionCount());
		map.put("spanYongGcCollectionTime", spanYongGcCollectionTime());
		map.put("spanFullGcCollectionCount", spanFullGcCollectionCount());
		map.put("spanFullGcCollectionTime", spanFullGcCollectionTime());
		return map;
	}
	
}
