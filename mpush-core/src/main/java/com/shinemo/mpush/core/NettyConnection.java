package com.shinemo.mpush.core;

import com.shinemo.mpush.api.SessionContext;
import com.shinemo.mpush.core.security.CipherBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    private SessionContext context;
    private Channel channel;
    private int status = 0;

    private int hbTimes;

    @Override
    public void init(Channel channel) {
        this.channel = channel;
        this.context = new SessionContext();
        this.context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
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
    public void send(final Packet packet) {
        if (packet != null) {
            if (channel.isWritable()) {
                ChannelFuture wf = channel.writeAndFlush(packet);
                wf.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            if (!channel.isActive()) {
                                LOGGER.warn("send msg failed, channel is not active clientIp={}, packet={}", channel.remoteAddress().toString(), packet);
                                ConnectionManager.INSTANCE.remove(channel);
                                channel.close();
                            }
                            LOGGER.warn("send msg failed clientIp={}, packet={}", channel.remoteAddress().toString(), packet);
                        } else {
                            LOGGER.warn("send msg success clientIp={}, packet={}", channel.remoteAddress().toString(), packet);
                        }
                    }
                });
            } else {
                LOGGER.warn("send msg failed, channel is not writable clientIp={}, packet={}", channel.remoteAddress().toString(), packet);
            }
        }
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int increaseAndGetHbTimes() {
        return ++hbTimes;
    }

    public void resetHbTimes() {
        hbTimes = 0;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void close() {
        this.channel.close();
    }

    @Override
    public int getHbTimes() {
        return hbTimes;
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }

    @Override
    public boolean isEnable() {
        return channel.isWritable();
    }

    @Override
    public String remoteIp() {
        return channel.remoteAddress().toString();
    }


    @Override
    public String toString() {
        return "NettyConnection{" +
                "context=" + context +
                ", channel=" + channel +
                ", status=" + status +
                '}';
    }
}
