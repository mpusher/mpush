package com.shinemo.mpush.core.task;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.netty.util.NettySharedHolder;

/**
 * 定时全量扫描connection
 */
public class ScanAllClientConnection implements TimerTask {

    private static final Logger log = LoggerFactory.getLogger(ScanAllClientConnection.class);

    private final List<ScanTask> taskList = new ArrayList<ScanTask>();

    public ScanAllClientConnection(final ScanTask... scanTasks) {
        if (scanTasks != null) {
            for (final ScanTask task : scanTasks) {
                this.taskList.add(task);
            }
        }
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        try {
            final long now = System.currentTimeMillis();
            List<Connection> connections = null; //NettyConnectionManager.INSTANCE.getConnections();
            if (connections != null) {
                for (Connection conn : connections) {
                    for (ScanTask task : this.taskList) {
                        task.visit(now, conn);
                    }
                }
            }
        } catch (Exception e) {
            log.error("", "exception on scan", e);
        } finally {
            NettySharedHolder.HASHED_WHEEL_TIMER.newTimeout(this, Constants.TIME_DELAY, TimeUnit.SECONDS);
        }
    }

}
