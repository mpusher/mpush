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

import com.mpush.core.handler.AdminHandler;
import com.mpush.netty.server.NettyServer;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.Executor;

public final class AdminServer extends NettyServer {
    private final ConnectionServer connectionServer;
    private final GatewayServer gatewayServer;

    private final AdminHandler adminHandler;

    public AdminServer(int port, ConnectionServer connectionServer, GatewayServer gatewayServer) {
        super(port);
        this.connectionServer = connectionServer;
        this.gatewayServer = gatewayServer;
        this.adminHandler = new AdminHandler(this);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        super.initPipeline(pipeline);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return adminHandler;
    }

    @Override
    protected ChannelHandler getDecoder() {
        return new StringDecoder();
    }

    @Override
    protected ChannelHandler getEncoder() {
        return new StringEncoder();
    }

    public ConnectionServer getConnectionServer() {
        return connectionServer;
    }

    public GatewayServer getGatewayServer() {
        return gatewayServer;
    }
}
