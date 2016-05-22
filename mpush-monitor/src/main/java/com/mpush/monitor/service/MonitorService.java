package com.mpush.monitor.service;

import com.mpush.api.BaseService;
import com.mpush.tools.log.Logs;
import com.mpush.monitor.data.MonitorResult;
import com.mpush.monitor.data.ResultCollector;
import com.mpush.monitor.quota.impl.JVMInfo;
import com.mpush.tools.JVMUtil;
import com.mpush.tools.Jsons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorService extends BaseService implements Runnable {

    private final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    public static final MonitorService I = new MonitorService();

    private static final int firstJstack = 2, secondJstack = 4, thirdJstack = 6, firstJmap = 4;

    private String dumpLogDir = "/tmp/logs/mpush/";
    private boolean dumpFirstJstack = false;
    private boolean dumpSecondJstack = false;
    private boolean dumpThirdJstack = false;
    private boolean dumpJmap = false;
    private boolean enableDump = false;

    private final ResultCollector collector = new ResultCollector();

    @Override
    public void run() {
        while (started.get()) {
            MonitorResult result = collector.collect();
            Logs.Monitor.info(Jsons.toJson(result));
            if (enableDump) {
                dump();
            }
            try {//30s
                Thread.sleep(30000L);
            } catch (InterruptedException e) {
                LOGGER.warn("monitor data exception", e);
            }
        }
    }

    @Override
    public void start(Listener listener) {
        if (started.compareAndSet(false, true)) {
            Thread thread = new Thread(this, "mp-t-monitor");
            thread.start();
        }
    }

    @Override
    public void stop(Listener listener) {
        started.set(false);
    }

    @Override
    public boolean isRunning() {
        return started.get();
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


    public MonitorService setDumpLogDir(String dumpLogDir) {
        this.dumpLogDir = dumpLogDir;
        return this;
    }

    public MonitorService setEnableDump(boolean enableDump) {
        this.enableDump = enableDump;
        return this;
    }
}
