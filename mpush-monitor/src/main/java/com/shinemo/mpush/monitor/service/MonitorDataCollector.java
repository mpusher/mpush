package com.shinemo.mpush.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.monitor.domain.MonitorData;
import com.shinemo.mpush.monitor.quota.impl.JVMGC;
import com.shinemo.mpush.monitor.quota.impl.JVMInfo;
import com.shinemo.mpush.monitor.quota.impl.JVMMemory;
import com.shinemo.mpush.monitor.quota.impl.JVMThread;
import com.shinemo.mpush.monitor.quota.impl.JVMThreadPool;
import com.shinemo.mpush.tools.JVMUtil;
import com.shinemo.mpush.tools.Jsons;

public class MonitorDataCollector {

    private static final Logger log = LoggerFactory.getLogger(MonitorDataCollector.class);

    private static volatile boolean dumpFirstJstack = false;

    private static volatile boolean dumpSecondJstack = false;

    private static volatile boolean dumpThirdJstack = false;

    private static volatile boolean dumpJmap = false;

    private static String currentPath = "/tmp/logs/mpush/";

    private static int firstJstack = 2;

    private static int secondJstack = 4;

    private static int thirdJstack = 6;

    private static int firstJmap = 4;

    public static MonitorData collect() {
        MonitorData data = new MonitorData();
        data.setInfoMap(JVMInfo.instance.toMap());
        data.setGcMap(JVMGC.instance.toMap());
        data.setMemoryMap(JVMMemory.instance.toMap());
        data.setThreadMap(JVMThread.instance.toMap());
        data.setThreadPoolMap(JVMThreadPool.instance.toMap());
        return data;
    }

    public void setPath(String path) {
        currentPath = path;
    }

    public static void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    MonitorData monitorData = MonitorDataCollector.collect();
                    log.error("monitor data:" + Jsons.toJson(monitorData));

                    double load = JVMInfo.instance.load();
                    if (load > firstJstack) {
                        if (!dumpFirstJstack) {
                            dumpFirstJstack = true;
                            JVMUtil.dumpJstack(currentPath);
                        }
                    }

                    if (load > secondJstack) {
                        if (!dumpSecondJstack) {
                            dumpSecondJstack = true;
                            JVMUtil.dumpJmap(currentPath);
                        }
                    }

                    if (load > thirdJstack) {
                        if (!dumpThirdJstack) {
                            dumpThirdJstack = true;
                            JVMUtil.dumpJmap(currentPath);
                        }
                    }

                    if (load > firstJmap) {
                        if (!dumpJmap) {
                            dumpJmap = true;
                            JVMUtil.dumpJmap(currentPath);
                        }
                    }

                    try {//30s
                        Thread.sleep(30000L);
                    } catch (InterruptedException e) {
                        log.warn("monitor data exception", e);
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        MonitorDataCollector.start();
    }

}
