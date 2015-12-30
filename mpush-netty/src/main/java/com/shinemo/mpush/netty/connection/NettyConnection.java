package com.shinemo.mpush.netty.connection;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.security.CipherBox;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/22.
 */
public final class NettyConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);

    private SessionContext context;
    private Channel channel;
    private boolean security;
    private volatile int status = 0;

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.security = security;
        this.context = new SessionContext();
        if (security) {
            this.context.changeCipher(CipherBox.INSTANCE.getRsaCipher());
        }
        this.status = 1;
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
    public ChannelFuture send(final Packet packet) {
        return channel.writeAndFlush(packet);
    }

    @Override
    public void send(Packet packet, ChannelFutureListener listener) {
        if (listener == null) channel.writeAndFlush(packet);
        else channel.writeAndFlush(packet).addListener(listener);
    }

    @Override
    public Channel channel() {
        return channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void close() {
        this.status = 0;
        this.channel.close();
    }

    @Override
    public boolean isConnected() {
        return status == 0 || channel.isActive();
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
