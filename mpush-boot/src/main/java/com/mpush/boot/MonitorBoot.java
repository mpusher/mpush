package com.mpush.boot;

import com.mpush.monitor.service.MonitorDataCollector;
import com.mpush.tools.config.ConfigCenter;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public class MonitorBoot extends BootJob {
    @Override
    void run() {
        MonitorDataCollector.start(ConfigCenter.I.skipDump());
        next();
    }
}
