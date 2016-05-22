package com.mpush.tools.thread.pool;

import com.mpush.tools.JVMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class DumpThreadRejectedHandler implements RejectedExecutionHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(DumpThreadRejectedHandler.class);

    private volatile boolean dumping = false;

    private static final String preFixPath = "/tmp/mpush/logs/dump/";

    private final ThreadPoolConfig context;

    public DumpThreadRejectedHandler(ThreadPoolConfig context) {
        this.context = context;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!dumping) {
            dumping = true;
            dumpJVMInfo();
        }
        throw new RejectedExecutionException();
    }

    private void dumpJVMInfo() {
        LOGGER.error("start dump jvm info");
        JVMUtil.dumpJstack(preFixPath + "/" + context.getName());
        LOGGER.error("end dump jvm info");
    }
}

