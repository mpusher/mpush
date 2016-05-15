package com.mpush.monitor.quota.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mpush.monitor.quota.BaseQuota;
import com.mpush.monitor.quota.ThreadQuota;

public class JVMThread extends BaseQuota implements ThreadQuota{

	private ThreadMXBean  threadMXBean;
	
	public static final JVMThread instance = new JVMThread();
	
	private JVMThread() {
		threadMXBean = ManagementFactory.getThreadMXBean();
	}
	
	@Override
	public int daemonThreadCount() {
		return threadMXBean.getDaemonThreadCount();
	}

	@Override
	public int threadCount() {
		return threadMXBean.getThreadCount();
	}

	@Override
	public long totalStartedThreadCount() {
		return threadMXBean.getTotalStartedThreadCount();
	}

	@Override
	public int deadLockedThreadCount() {
		try {
            long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadLockedThreadIds == null) {
                return 0;
            }
            return deadLockedThreadIds.length;
        } catch (Exception e) {
        	return 0;
        }
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("daemonThreadCount", daemonThreadCount());
		map.put("threadCount", threadCount());
		map.put("totalStartedThreadCount", totalStartedThreadCount());
		map.put("deadLockedThreadCount", deadLockedThreadCount());
		return map;
	}

	
}
