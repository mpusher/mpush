/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.client.gateway;


import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.client.push.PushRequest;
import com.mpush.client.push.PushRequestBus;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.netty.connection.NettyConnection;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mpush.common.ErrorCode.*;

/**
 * Created by ohun on 2015/12/19.
 *
 * @author ohun@live.cn
 */
@ChannelHandler.Sharable
public final class GatewayClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayClientChannelHandler.class);

    private final Connection connection = new NettyConnection();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("receive gateway packet={}, channel={}", msg, ctx.channel());
        connection.updateLastReadTime();
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            if (packet.cmd == Command.OK.cmd) {
                handleOK(new OkMessage(packet, connection));
            } else if (packet.cmd == Command.ERROR.cmd) {
                handleError(new ErrorMessage(packet, connection));
            }
        }
    }

    private void handleOK(OkMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            handPush(message, null, message.getPacket());
        }
    }

    private void handleError(ErrorMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            handPush(null, message, message.getPacket());
        }
    }

    private void handPush(OkMessage ok, ErrorMessage error, Packet packet) {
        PushRequest request = PushRequestBus.I.getAndRemove(packet.sessionId);
        if (request == null) {
            LOGGER.warn("receive a gateway response, but request has timeout. ok={}, error={}", ok, error);
            return;
        }

        if (ok != null) {//推送成功
            request.success(ok.data);
        } else if (error != null) {//推送失败
            LOGGER.warn("receive an error gateway response, message={}", error);
            if (error.code == OFFLINE.errorCode) {//用户离线
                request.offline();
            } else if (error.code == PUSH_CLIENT_FAILURE.errorCode) {//下发到客户端失败
                request.failure();
            } else if (error.code == ROUTER_CHANGE.errorCode) {//用户路由信息更改
                request.redirect();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connection.close();
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client connect channel={}", ctx.channel());
        connection.init(ctx.channel(), false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connection.close();
        LOGGER.info("client disconnect channel={}", ctx.channel());
        //TODO notify gateway-client-factory to removeAndClose this gateway-client
    }

    public Connection getConnection() {
        return connection;
    }
}