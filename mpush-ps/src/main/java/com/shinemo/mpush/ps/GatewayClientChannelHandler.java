package com.shinemo.mpush.ps;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.netty.connection.NettyConnection;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shinemo.mpush.common.ErrorCode.OFFLINE;
import static com.shinemo.mpush.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.shinemo.mpush.common.ErrorCode.ROUTER_CHANGE;

/**
 * Created by ohun on 2015/12/30.
 */
public class GatewayClientChannelHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayClientChannelHandler.class);
    private final Connection connection = new NettyConnection();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection.init(ctx.channel(), false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connection.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
                ErrorMessage message = new ErrorMessage(packet, connection);
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
    }

    public Connection getConnection() {
        return connection;
    }
}
