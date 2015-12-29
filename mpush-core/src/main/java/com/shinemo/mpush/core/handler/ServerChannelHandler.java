package com.shinemo.mpush.core.handler;


import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.PacketReceiver;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.NettyConnection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    private final PacketReceiver receiver;

    public ServerChannelHandler(PacketReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = ConnectionManager.INSTANCE.get(ctx.channel());
        receiver.onReceive((Packet) msg, connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ConnectionManager.INSTANCE.remove(ctx.channel());
        LOGGER.error(ctx.channel().remoteAddress() + ", exceptionCaught", cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn(ctx.channel().remoteAddress() + ",  channelActive");
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        ConnectionManager.INSTANCE.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn(ctx.channel().remoteAddress() + ",  channelInactive");
        ConnectionManager.INSTANCE.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            switch (stateEvent.state()) {
                case READER_IDLE:
                    ConnectionManager.INSTANCE.remove(ctx.channel());
                    ctx.close();
                    LOGGER.warn("heartbeat read timeout, chanel closed!");
                    break;
                case WRITER_IDLE:
                    ctx.writeAndFlush(Packet.getHBPacket());
                    LOGGER.warn("heartbeat write timeout, do write an EOL.");
                    break;
                case ALL_IDLE:
            }
        } else {
            LOGGER.warn("One user event Triggered. evt=" + evt);
            super.userEventTriggered(ctx, evt);
        }
    }
}