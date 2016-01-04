package com.shinemo.mpush.client;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.ErrorCode;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.netty.connection.NettyConnection;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by ohun on 2015/12/30.
 */
public class PushClientChannelHandler extends ChannelHandlerAdapter {
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
                return;
            }

            if (packet.cmd == Command.OK.cmd) {
                request.success();
            } else if (packet.cmd == Command.ERROR.cmd) {
                ErrorMessage message = new ErrorMessage(packet, connection);
                byte errorCode = message.code;
                if (errorCode == ErrorCode.OFFLINE.errorCode) {
                    request.offline();
                } else if (errorCode == ErrorCode.PUSH_CLIENT_FAILURE.errorCode) {
                    request.failure();
                } else if (errorCode == ErrorCode.ROUTER_CHANGE.errorCode) {
                    request.redirect();
                }
            } else {

            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
