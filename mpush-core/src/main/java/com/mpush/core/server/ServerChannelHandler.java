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

package com.mpush.core.server;


import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.netty.connection.NettyConnection;
import com.mpush.tools.common.Profiler;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 *
 * @author ohun@live.cn
 */
@ChannelHandler.Sharable
public final class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    /**
     * 是否启用加密
     */
    private final boolean security;
    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public ServerChannelHandler(boolean security, ConnectionManager connectionManager, PacketReceiver receiver) {
        this.security = security;
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Profiler.start("channel read:");
            Connection connection = connectionManager.get(ctx.channel());
            LOGGER.debug("channelRead channel={}, connection={}, packet={}", ctx.channel(), connection, msg);
            connection.updateLastReadTime();
            receiver.onReceive((Packet) msg, connection);
        } finally {
            Profiler.release();
            long duration = Profiler.getDuration();
            if (duration > 80) {
                LOGGER.error("channel read busy:" + duration + "," + Profiler.dump());
            }
            Profiler.reset();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        Logs.Conn.error("client exceptionCaught channel={}, connection={}", ctx.channel(), connection);
        LOGGER.error("caught an ex, channel={}, connection={}", ctx.channel(), connection, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.Conn.info("client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.I.post(new ConnectionCloseEvent(connection));
        Logs.Conn.info("client disconnect channel={}, connection={}", ctx.channel(), connection);
    }
}