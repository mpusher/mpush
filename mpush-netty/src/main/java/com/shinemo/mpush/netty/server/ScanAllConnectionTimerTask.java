package com.shinemo.mpush.netty.server;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.netty.util.NettySharedHolder;
import com.shinemo.mpush.tools.config.ConfigCenter;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class ScanAllConnectionTimerTask implements TimerTask{
	
	private static final Logger log = LoggerFactory.getLogger(ScanAllConnectionTimerTask.class);
	
	private ConnectionManager connectionManager;
	
	private static int maxHBTimeoutTimes = ConfigCenter.holder.maxHBTimeoutTimes();
	
	// 180S也许是个不错的选择，微信的心跳时间为300S
	private static long scanConnTaskCycle = ConfigCenter.holder.scanConnTaskCycle();
	
	public ScanAllConnectionTimerTask(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@Override
	public void run(Timeout timeout) throws Exception {
		
		try {
            long now = System.currentTimeMillis();
            List<Connection> connections = connectionManager.getConnections();
            log.warn("start deal ScanAllConnectionTimerTask:size,"+connections.size());
            if (connections != null) {
                for (final Connection conn : connections) {
                	if (!conn.isConnected()) {
                		log.warn("connect is not connected: "+conn);
                		return;
                	}
                	long betwen = now - conn.getLastReadTime();
                    if (betwen > scanConnTaskCycle) {
                    	int expiredTimes = conn.inceaseAndGetHbTimes();
                        if (expiredTimes > maxHBTimeoutTimes) {
                        	conn.close();
                            log.error("connection heartbeat timeout, connection has bean closed:"+conn);
                        } else {
                            log.error("connection heartbeat timeout, expiredTimes=" + expiredTimes+","+conn);
                        }
                    } else {
                    	conn.resetHbTimes();
                    	log.warn("connection heartbeat reset, expiredTimes=0,betwen:"+betwen+","+conn+",lastReadTime:"+conn.getLastReadTime()+",now:"+now);
                    }
                }
            }
        } catch (Throwable e) {
            log.error("error during ScanAllConnectionTimerTask", e);
        } finally {
            NettySharedHolder.HASHED_WHEEL_TIMER.newTimeout(this, scanConnTaskCycle/1000, TimeUnit.SECONDS);
        }
		
	}

}
