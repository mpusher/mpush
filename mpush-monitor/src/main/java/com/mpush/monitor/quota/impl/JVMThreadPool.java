package com.mpush.monitor.quota.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.collect.Maps;
import com.mpush.monitor.quota.BaseQuota;
import com.mpush.monitor.quota.ThreadPoolQuota;
import com.mpush.tools.thread.threadpool.ThreadPoolManager;

public class JVMThreadPool extends BaseQuota implements ThreadPoolQuota{

	public static final JVMThreadPool instance = new JVMThreadPool();
	
	private JVMThreadPool() {
	}
	
	private Map<String, Executor> pool = null;
	
	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> map = Maps.newHashMap();
		if(pool == null){
			pool = ThreadPoolManager.getPool();
		}
		
		Iterator<Map.Entry<String, Executor>> ite = pool.entrySet().iterator();
		while (ite.hasNext()) {
			Map.Entry<String, Executor> entry = ite.next();
			String serviceUniqName = entry.getKey();
			ThreadPoolExecutor executor = (ThreadPoolExecutor)entry.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append("coreThreadNums:" + executor.getCorePoolSize() + " maxThreadNums:" + executor.getMaximumPoolSize() + " activityThreadNums:"
					+ executor.getActiveCount());
			map.put(serviceUniqName, sb.toString());
		}
		return map;
	}
	
	

}
