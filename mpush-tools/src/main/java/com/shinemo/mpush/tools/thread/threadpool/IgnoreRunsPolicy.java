package com.shinemo.mpush.tools.thread.threadpool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.JVMUtil;

public class IgnoreRunsPolicy implements RejectedExecutionHandler{

	private final static Logger log = LoggerFactory.getLogger(IgnoreRunsPolicy.class);

	private volatile boolean dump = false;
	
	private ThreadPoolContext context;
	
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
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    String logPath = Constants.JVM_LOG_PATH;
                    FileOutputStream jstackStream = null;
                    try {
                        jstackStream = new FileOutputStream(new File(logPath, "jstack.log"));
                        JVMUtil.jstack(jstackStream);
                    } catch (FileNotFoundException e) {
                    	log.error("", "Dump JVM cache Error!", e);
                    } catch (Throwable t) {
                    	log.error("", "Dump JVM cache Error!", t);
                    } finally {
                        if (jstackStream != null) {
                            try {
                                jstackStream.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            });

        }
	}
}

