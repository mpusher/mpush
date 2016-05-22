package com.mpush.monitor.quota.impl;

import com.google.common.collect.Maps;
import com.mpush.monitor.quota.BaseQuota;
import com.mpush.monitor.quota.ThreadPoolQuota;
import com.mpush.tools.thread.pool.ThreadPoolManager;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class JVMThreadPool extends BaseQuota implements ThreadPoolQuota {
    public static final JVMThreadPool I = new JVMThreadPool();

    private JVMThreadPool() {
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        Map<String, Executor> pool = ThreadPoolManager.I.getActivePools();
        for (Map.Entry<String, Executor> entry : pool.entrySet()) {
            String serviceName = entry.getKey();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) entry.getValue();
            String info = "coreThreadNum:" + executor.getCorePoolSize()
                    + " maxThreadNum:" + executor.getMaximumPoolSize()
                    + " workingThreadNum:" + executor.getActiveCount()
                    + " workThreadNum:" + executor.getPoolSize()
                    + " blockTaskNum:" + executor.getQueue().size();
            map.put(serviceName, info);
        }
        return map;
    }


}
