package com.mpush.core.server;

import com.mpush.api.message.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.netty.codec.PacketDecoder;
import com.mpush.netty.connection.NettyConnection;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;
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
@ChannelHandler.Sharable
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketChannelHandler.class);
    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public WebSocketChannelHandler(ConnectionManager connectionManager, PacketReceiver receiver) {
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            Connection connection = connectionManager.get(ctx.channel());
            Packet packet = PacketDecoder.decodeFrame(text);
            LOGGER.debug("channelRead conn={}, packet={}", ctx.channel(), connection.getSessionContext(), packet);
            receiver.onReceive(packet, connection);
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        Logs.CONN.error("client caught ex, conn={}", connection);
        LOGGER.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("client disconnected conn={}", connection);
    }
}