package com.mpush.core.server;

import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.ConnectionManager;
import com.mpush.netty.connection.NettyConnection;
import com.shinemo.signin.amc.common.PacketReceiver;
import com.shinemo.signin.amc.server.conn.ConnManager;
import com.shinemo.signin.amc.server.conn.Connection;
import com.shinemo.signin.amc.server.message.MessageReceiver;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final Logger logger = LoggerFactory.getLogger(WebSocketChannelHandler.class.getSimpleName());

    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public WebSocketChannelHandler(ConnectionManager connectionManager, PacketReceiver receiver) {
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            receiver.onReceive(request, ctx.channel());
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
        logger.info("connect active, channel={}", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionManager.removeAndClose(ctx.channel());
        super.channelInactive(ctx);
        logger.info("connect inactive, channel={}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connectionManager.removeAndClose(ctx.channel());
        super.exceptionCaught(ctx, cause);
        logger.info("connect ex, channel={}", ctx.channel(), cause);
    }
}