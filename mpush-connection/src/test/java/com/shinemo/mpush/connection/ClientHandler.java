package com.shinemo.mpush.connection;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientHandler extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Packet packet = new Packet();
        packet.command = Command.Handshake.cmd;
        packet.version = 0;
        packet.flags = 0;
        packet.msgId = 1;
        packet.body = "hello word".getBytes(Constants.UTF_8);
        ctx.writeAndFlush(packet);
        logger.info("client,"+ctx.channel().remoteAddress().toString(),"channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("client,"+ctx.channel().remoteAddress().toString(),"channelInactive");
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        logger.info("client,"+ctx.channel().remoteAddress().toString(),"channelRead",msg);
    }
}
