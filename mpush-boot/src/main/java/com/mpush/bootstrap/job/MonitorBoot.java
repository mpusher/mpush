package com.mpush.bootstrap.job;

import com.mpush.monitor.service.MonitorService;
import com.mpush.tools.config.CC;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public class MonitorBoot extends BootJob {
    @Override
    void run() {
        MonitorService.I
                .setEnableDump(CC.mp.monitor.dump_stack)
                .setDumpLogDir(CC.mp.log_dir)
                .start();
        next();
    }
}
