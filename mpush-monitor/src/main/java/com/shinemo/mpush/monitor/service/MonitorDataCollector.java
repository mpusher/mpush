package com.shinemo.mpush.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.monitor.domain.MonitorData;
import com.shinemo.mpush.monitor.mbean.impl.JVMGC;
import com.shinemo.mpush.monitor.mbean.impl.JVMMemory;
import com.shinemo.mpush.monitor.mbean.impl.JVMThread;
import com.shinemo.mpush.tools.Jsons;

public class MonitorDataCollector {
	
	private static final Logger log = LoggerFactory.getLogger(MonitorDataCollector.class);
	
	public static MonitorData collect(){
		MonitorData data = new MonitorData();
		data.setGcMap(JVMGC.instance.toMap());
		data.setMemoryMap(JVMMemory.instance.toMap());
		data.setThreadMap(JVMThread.instance.toMap());
		return data;
	}
	
	public static void start(){
		new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                	MonitorData monitorData = MonitorDataCollector.collect();
                	log.warn("monitor data:"+Jsons.toJson(monitorData));
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    	log.warn("monitor data exception",e);
                    }
                }
            }
        }).start();
	}
	
}
