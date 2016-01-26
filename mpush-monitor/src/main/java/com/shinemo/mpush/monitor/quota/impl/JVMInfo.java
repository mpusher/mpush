package com.shinemo.mpush.monitor.quota.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import com.google.common.collect.Maps;
import com.shinemo.mpush.monitor.quota.BaseQuota;
import com.shinemo.mpush.monitor.quota.InfoQuota;

public class JVMInfo extends BaseQuota implements InfoQuota{
	
	public static final JVMInfo instance = new JVMInfo();
	
	private RuntimeMXBean runtimeMXBean;
	
	private OperatingSystemMXBean systemMXBean;
	
	private String currentPid;
	
	private JVMInfo() {
		runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		systemMXBean = ManagementFactory.getOperatingSystemMXBean();
	}

	@Override
	public String pid() {
		if (null == currentPid) {
			currentPid = runtimeMXBean.getName().split("@")[0];
        }
        return currentPid;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("pid", pid());
		map.put("load", load());
		return map;
	}

	@Override
	public double load() {
		double averageLoad = systemMXBean.getSystemLoadAverage();
		return averageLoad;
	}

}
