/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.core.server;

import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.protocol.Command;
import com.mpush.api.service.Listener;
import com.mpush.api.spi.handler.PushHandlerFactory;
import com.mpush.common.MessageDispatcher;
import com.mpush.core.MPushServer;
import com.mpush.core.handler.AckHandler;
import com.mpush.core.handler.BindUserHandler;
import com.mpush.core.handler.HandshakeHandler;
import com.mpush.netty.server.NettyTCPServer;
import com.mpush.tools.config.CC;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

/**
 * Created by ohun on 2016/12/16.
 *
 * @author ohun@live.cn (夜色)
 */
public final class WebsocketServer extends NettyTCPServer {

    private final ChannelHandler channelHandler;

    private final MessageDispatcher messageDispatcher;

    private final ConnectionManager connectionManager;

    private final MPushServer mPushServer;

    public WebsocketServer(MPushServer mPushServer) {
        super(CC.mp.net.ws_server_port);
        this.mPushServer = mPushServer;
        this.messageDispatcher = new MessageDispatcher();
        this.connectionManager = new ServerConnectionManager(false);
        this.channelHandler = new WebSocketChannelHandler(connectionManager, messageDispatcher);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        messageDispatcher.register(Command.HANDSHAKE, () -> new HandshakeHandler(mPushServer));
        messageDispatcher.register(Command.BIND, () -> new BindUserHandler(mPushServer));
        messageDispatcher.register(Command.UNBIND, () -> new BindUserHandler(mPushServer));
        messageDispatcher.register(Command.PUSH, PushHandlerFactory::create);
        messageDispatcher.register(Command.ACK, () -> new AckHandler(mPushServer));
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        connectionManager.destroy();
    }

    @Override
    public EventLoopGroup getBossGroup() {
        return mPushServer.getConnectionServer().getBossGroup();
    }

    @Override
    public EventLoopGroup getWorkerGroup() {
        return mPushServer.getConnectionServer().getWorkerGroup();
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(CC.mp.net.ws_path, null, true));
        pipeline.addLast(new WebSocketIndexPageHandler());
        pipeline.addLast(getChannelHandler());
    }

    @Override
    protected void initOptions(ServerBootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
        b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
