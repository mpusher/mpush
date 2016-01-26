package com.shinemo.mpush.tools.thread.threadpool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.JVMUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;

public class IgnoreRunsPolicy implements RejectedExecutionHandler{

	private final static Logger log = LoggerFactory.getLogger(IgnoreRunsPolicy.class);

	private volatile boolean dump = false;
	
	private static final String preFixPath = ConfigCenter.holder.logPath();
	
	private final ThreadPoolContext context;
	
	public IgnoreRunsPolicy(ThreadPoolContext context) {
		this.context = context;
	}
	
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		dumpJVMInfo();
		throw new RejectedExecutionException();
	}
	
	private void dumpJVMInfo(){
		if (!dump) {
			dump = true;
			log.error("start dump jvm info");
			JVMUtil.dumpJstack(preFixPath+"/"+context.getName());
			log.error("end dump jvm info");
        }
	}
}

