package com.mpush.push.client;



import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.api.connection.Connection;
import com.mpush.api.Client;
import com.mpush.common.message.ErrorMessage;
import com.mpush.netty.client.ChannelClientHandler;
import com.mpush.netty.connection.NettyConnection;
import com.mpush.push.PushRequest;
import com.mpush.push.PushRequestBus;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mpush.common.ErrorCode.OFFLINE;
import static com.mpush.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.mpush.common.ErrorCode.ROUTER_CHANGE;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public final class ClientChannelHandler extends ChannelHandlerAdapter implements ChannelClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);
    
    private Client client;
    
    public ClientChannelHandler(Client client) {
    	this.client = client;
	}
    
    @Override
    public Client getClient() {
    	return client;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        client.getConnection().updateLastReadTime();
        if (msg instanceof Packet) {
            Packet packet = ((Packet) msg);
            PushRequest request = PushRequestBus.INSTANCE.remove(packet.sessionId);
            if (request == null) {
                LOGGER.warn("receive a gateway response, but request timeout. packet={}", packet);
                return;
            }

            if (packet.cmd == Command.OK.cmd) {
                request.success();
            } else {
                ErrorMessage message = new ErrorMessage(packet, client.getConnection());
                if (message.code == OFFLINE.errorCode) {
                    request.offline();
                } else if (message.code == PUSH_CLIENT_FAILURE.errorCode) {
                    request.failure();
                } else if (message.code == ROUTER_CHANGE.errorCode) {
                    request.redirect();
                }
                LOGGER.warn("receive an error gateway response, message={}", message);
            }
        }
    	LOGGER.warn("update currentTime:"+ctx.channel()+","+ToStringBuilder.reflectionToString(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	client.close("exception");
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), false);
        client.initConnection(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	client.close("inactive");
        LOGGER.info("client disconnect channel={}", ctx.channel());
    }

    
}