package com.mpush.monitor.data;

import com.mpush.monitor.quota.impl.*;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public class ResultCollector {

    public MonitorResult collect() {
        MonitorResult data = new MonitorResult();
        data.setInfoMap(JVMInfo.I.toMap());
        data.setGcMap(JVMGC.I.toMap());
        data.setMemoryMap(JVMMemory.I.toMap());
        data.setThreadMap(JVMThread.I.toMap());
        data.setThreadPoolMap(JVMThreadPool.I.toMap());
        return data;
    }

}
