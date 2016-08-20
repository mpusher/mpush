/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.monitor.service;

import com.mpush.api.service.BaseService;
import com.mpush.api.service.Listener;
import com.mpush.monitor.data.MonitorResult;
import com.mpush.monitor.data.ResultCollector;
import com.mpush.monitor.quota.impl.JVMInfo;
import com.mpush.tools.common.JVMUtil;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MonitorService extends BaseService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final MonitorService I = new MonitorService();

    private static final int firstJstack = 2, secondJstack = 4, thirdJstack = 6, firstJmap = 4;

    private static final String dumpLogDir = CC.mp.monitor.dump_dir;
    private static final boolean dumpEnabled = CC.mp.monitor.dump_stack;
    private static final boolean printLog = CC.mp.monitor.print_log;
    private static final long dumpPeriod = CC.mp.monitor.dump_period.getSeconds();

    private boolean dumpFirstJstack = false;
    private boolean dumpSecondJstack = false;
    private boolean dumpThirdJstack = false;
    private boolean dumpJmap = false;

    private final ResultCollector collector = new ResultCollector();

    @Override
    public void run() {
        while (isRunning()) {
            MonitorResult result = collector.collect();

            if (printLog) {
                Logs.Monitor.info(result.toJson());
            }

            if (dumpEnabled) {
                dump();
            }

            try {
                TimeUnit.SECONDS.sleep(dumpPeriod);
            } catch (InterruptedException e) {
                stop();
            }
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        if (printLog || dumpEnabled) {
            Thread thread = new Thread(this, "mp-t-monitor");
            thread.start();
        }
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        logger.error("monitor service stopped!");
    }

    private void dump() {
        double load = JVMInfo.I.load();
        if (load > firstJstack) {
            if (!dumpFirstJstack) {
                dumpFirstJstack = true;
                JVMUtil.dumpJstack(dumpLogDir);
            }
        }

        if (load > secondJstack) {
            if (!dumpSecondJstack) {
                dumpSecondJstack = true;
                JVMUtil.dumpJmap(dumpLogDir);
            }
        }

        if (load > thirdJstack) {
            if (!dumpThirdJstack) {
                dumpThirdJstack = true;
                JVMUtil.dumpJmap(dumpLogDir);
            }
        }

        if (load > firstJmap) {
            if (!dumpJmap) {
                dumpJmap = true;
                JVMUtil.dumpJmap(dumpLogDir);
            }
        }
    }
}
