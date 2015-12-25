package com.shinemo.mpush.connection;


import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.ConnectionManager;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.NettyConnection;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
public class ConnectionHandler extends ChannelHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MessageReceiver receiver = new MessageReceiver();
    private NettyConnection connection = new NettyConnection();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead msg=" + msg);
        logger.debug("channelRead msg=" + msg);
        receiver.onMessage((Packet) msg, connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ConnectionManager.INSTANCE.remove(connection);
        System.err.println("exceptionCaught");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        connection.init(ctx.channel());
        ConnectionManager.INSTANCE.add(connection);
        System.out.println("server receive channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionManager.INSTANCE.remove(connection);
        System.out.println("server receive channelInactive");
    }
}
