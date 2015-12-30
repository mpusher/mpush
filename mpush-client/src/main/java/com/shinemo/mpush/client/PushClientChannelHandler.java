package com.shinemo.mpush.client;

import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.netty.connection.NettyConnection;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushClientChannelHandler extends ChannelHandlerAdapter {
    private final Connection connection = new NettyConnection();
    private final PacketReceiver receiver;

    public PushClientChannelHandler(PacketReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection.init(ctx.channel(), false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        connection.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        receiver.onReceive(((Packet) msg), connection);
    }

    public Connection getConnection() {
        return connection;
    }
}
