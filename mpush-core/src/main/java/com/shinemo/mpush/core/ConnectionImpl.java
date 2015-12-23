package com.shinemo.mpush.core;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.ConnectionInfo;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionImpl implements Connection {
    private ConnectionInfo info;
    private Channel channel;
    private int status = 0;

    public void init(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public void send(Packet packet) {

    }

    public ChannelFuture close() {
        return null;
    }

    public ConnectionInfo getInfo() {
        return info;
    }

    public void setInfo(ConnectionInfo info) {
        this.info = info;
    }

    public Channel getChannel() {
        return channel;
    }
}
