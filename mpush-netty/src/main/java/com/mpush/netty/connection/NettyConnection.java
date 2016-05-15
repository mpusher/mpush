package com.mpush.netty.connection;

import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.protocol.Packet;
import com.mpush.common.security.CipherBox;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnection implements Connection, ChannelFutureListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    private SessionContext context;
    private Channel channel;
    private volatile int status = STATUS_NEW;
    private long lastReadTime;
	private long lastWriteTime;
    
    private int hbTimes = 0;

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.context = new SessionContext();
        this.lastReadTime = System.currentTimeMillis();
        this.status = STATUS_CONNECTED;
        if (security) {
            this.context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
        }
    }

    @Override
    public void setSessionContext(SessionContext context) {
        this.context = context;
    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public String getId() {
        return channel.id().asLongText();
    }

    @Override
    public ChannelFuture send(Packet packet) {
        return send(packet, null);
    }

    @Override
    public ChannelFuture send(Packet packet, final ChannelFutureListener listener) {
        if (channel.isActive() && channel.isWritable()) {
            if (listener != null) {
                return channel.writeAndFlush(packet).addListener(listener).addListener(this);
            } else {
                return channel.writeAndFlush(packet).addListener(this);
            }
        } else {
            return this.close();
            
        }
    }

    @Override
    public ChannelFuture close() {
        if (status == STATUS_DISCONNECTED) return null;
        this.status = STATUS_DISCONNECTED;
        return this.channel.close();
    }

    @Override
    public boolean isConnected() {
        return status == STATUS_CONNECTED || channel.isActive();
    }

    @Override
    public boolean heartbeatTimeout() {
    	long between = System.currentTimeMillis() - lastReadTime;
        return context.heartbeat > 0 && between > context.heartbeat;
    }

    @Override
    public void updateLastReadTime() {
        lastReadTime = System.currentTimeMillis();
    }
    
    @Override
    public long getLastReadTime(){
    	return lastReadTime;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            lastWriteTime = System.currentTimeMillis();
        } else {
            LOGGER.error("send msg error");
        }
    }
    
    public void updateLastWriteTime(){
    	lastWriteTime = System.currentTimeMillis();
    }
    
    @Override
    public int inceaseAndGetHbTimes() {
        return ++hbTimes;
    }

    @Override
    public void resetHbTimes() {
        hbTimes = 0;
    }

	@Override
	public String toString() {
		return "NettyConnection [context=" + context + ", channel=" + channel + ", status=" + status + ", lastReadTime=" + lastReadTime + ", lastWriteTime=" + lastWriteTime + ", hbTimes=" + hbTimes
				+ "]";
	}

	@Override
	public Channel getChannel() {
		return channel;
	}
    

}
