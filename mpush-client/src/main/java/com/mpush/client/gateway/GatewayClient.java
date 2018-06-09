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

import com.mpush.api.connection.ConnectionManager;
import com.mpush.api.protocol.Command;
import com.mpush.api.service.Listener;
import com.mpush.client.MPushClient;
import com.mpush.client.gateway.handler.GatewayClientChannelHandler;
import com.mpush.client.gateway.handler.GatewayErrorHandler;
import com.mpush.client.gateway.handler.GatewayOKHandler;
import com.mpush.common.MessageDispatcher;
import com.mpush.netty.client.NettyTCPClient;
import com.mpush.netty.connection.NettyConnectionManager;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.CC.mp.net.rcv_buf;
import com.mpush.tools.config.CC.mp.net.snd_buf;
import com.mpush.tools.thread.NamedPoolThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mpush.tools.config.CC.mp.net.traffic_shaping.gateway_client.*;
import static com.mpush.tools.thread.ThreadNames.T_TRAFFIC_SHAPING;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public class GatewayClient extends NettyTCPClient {
    private final GatewayClientChannelHandler handler;
    private GlobalChannelTrafficShapingHandler trafficShapingHandler;
    private ScheduledExecutorService trafficShapingExecutor;
    private final ConnectionManager connectionManager;
    private final MessageDispatcher messageDispatcher;

    public GatewayClient(MPushClient mPushClient) {
        messageDispatcher = new MessageDispatcher();
        messageDispatcher.register(Command.OK, () -> new GatewayOKHandler(mPushClient));
        messageDispatcher.register(Command.ERROR, () -> new GatewayErrorHandler(mPushClient));
        connectionManager = new NettyConnectionManager();
        handler = new GatewayClientChannelHandler(connectionManager, messageDispatcher);
        if (enabled) {
            trafficShapingExecutor = Executors.newSingleThreadScheduledExecutor(new NamedPoolThreadFactory(T_TRAFFIC_SHAPING));
            trafficShapingHandler = new GlobalChannelTrafficShapingHandler(
                    trafficShapingExecutor,
                    write_global_limit, read_global_limit,
                    write_channel_limit, read_channel_limit,
                    check_interval);
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        super.initPipeline(pipeline);
        if (trafficShapingHandler != null) {
            pipeline.addFirst(trafficShapingHandler);
        }
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (trafficShapingHandler != null) {
            trafficShapingHandler.release();
            trafficShapingExecutor.shutdown();
        }
        super.doStop(listener);
    }

    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        if (snd_buf.gateway_client > 0) b.option(ChannelOption.SO_SNDBUF, snd_buf.gateway_client);
        if (rcv_buf.gateway_client > 0) b.option(ChannelOption.SO_RCVBUF, rcv_buf.gateway_client);
    }

    @Override
    public ChannelFactory<? extends Channel> getChannelFactory() {
        if (CC.mp.net.tcpGateway()) return super.getChannelFactory();
        if (CC.mp.net.udtGateway()) return NioUdtProvider.BYTE_CONNECTOR;
        if (CC.mp.net.sctpGateway()) return NioSctpChannel::new;
        return super.getChannelFactory();
    }

    @Override
    public SelectorProvider getSelectorProvider() {
        if (CC.mp.net.tcpGateway()) return super.getSelectorProvider();
        if (CC.mp.net.udtGateway()) return NioUdtProvider.BYTE_PROVIDER;
        if (CC.mp.net.sctpGateway()) return super.getSelectorProvider();
        return super.getSelectorProvider();
    }

    @Override
    protected int getWorkThreadNum() {
        return CC.mp.thread.pool.gateway_client_work;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
