package com.shinemo.mpush.connection;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientHandler extends ChannelHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        System.out.println("client connect");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("client channelInactive");
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println(msg);
        logger.debug("channelRead msg=" + msg);
    }
}
