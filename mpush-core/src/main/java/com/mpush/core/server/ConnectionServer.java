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


import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.protocol.Command;
import com.mpush.api.service.Listener;
import com.mpush.common.MessageDispatcher;
import com.mpush.core.handler.*;
import com.mpush.netty.http.HttpClient;
import com.mpush.netty.http.NettyHttpClient;
import com.mpush.netty.server.NettyServer;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.mpush.tools.config.CC.mp.net.traffic_shaping.connect_server.*;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ConnectionServer extends NettyServer {
    private ServerChannelHandler channelHandler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;

    private ConnectionManager connectionManager = new ServerConnectionManager();
    private HttpClient httpClient;

    public ConnectionServer(int port) {
        super(port);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.HEARTBEAT, new HeartBeatHandler());
        receiver.register(Command.HANDSHAKE, new HandshakeHandler());
        receiver.register(Command.BIND, new BindUserHandler());
        receiver.register(Command.UNBIND, new BindUserHandler());
        receiver.register(Command.FAST_CONNECT, new FastConnectHandler());
        receiver.register(Command.ACK, new AckHandler());

        if (CC.mp.http.proxy_enabled) {
            httpClient = new NettyHttpClient();
            receiver.register(Command.HTTP_PROXY, new HttpProxyHandler(httpClient));
        }
        channelHandler = new ServerChannelHandler(true, connectionManager, receiver);

        if (enabled) {
            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                    Executors.newSingleThreadScheduledExecutor()
                    , write_global_limit, read_global_limit,
                    write_channel_limit, read_channel_limit,
                    check_interval);
        }
    }

    @Override
    public void stop(Listener listener) {
        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
        }
        super.stop(listener);
        if (httpClient != null && httpClient.isRunning()) {
            httpClient.stop();
        }
        connectionManager.destroy();
    }

    @Override
    protected Executor getWorkExecutor() {
        return ThreadPoolManager.I.getWorkExecutor();
    }

    @Override
    protected Executor getBossExecutor() {
        return ThreadPoolManager.I.getBossExecutor();
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addLast(trafficShapingHandler);
        }
    }

    @Override
    protected void initOptions(ServerBootstrap b) {
        super.initOptions(b);
        /***
         * 你可以设置这里指定的通道实现的配置参数。
         * 我们正在写一个TCP/IP的服务端，
         * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
         * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
         */
        b.option(ChannelOption.SO_BACKLOG, 1024);

        /**
         * TCP层面的接收和发送缓冲区大小设置，
         * 在Netty中分别对应ChannelOption的SO_SNDBUF和SO_RCVBUF，
         * 需要根据推送消息的大小，合理设置，对于海量长连接，通常32K是个不错的选择。
         */
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

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
