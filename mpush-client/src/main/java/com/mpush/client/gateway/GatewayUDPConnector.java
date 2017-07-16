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
import com.mpush.api.service.Listener;
import com.mpush.client.MPushClient;
import com.mpush.client.gateway.handler.GatewayErrorHandler;
import com.mpush.client.gateway.handler.GatewayOKHandler;
import com.mpush.common.MessageDispatcher;
import com.mpush.netty.udp.UDPChannelHandler;
import com.mpush.netty.udp.NettyUDPConnector;
import com.mpush.tools.Utils;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.CC.mp.net.rcv_buf;
import com.mpush.tools.config.CC.mp.net.snd_buf;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import static com.mpush.common.MessageDispatcher.POLICY_LOG;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class GatewayUDPConnector extends NettyUDPConnector {

    private UDPChannelHandler channelHandler;
    private MessageDispatcher messageDispatcher;
    private MPushClient mPushClient;

    public GatewayUDPConnector(MPushClient mPushClient) {
        super(CC.mp.net.gateway_client_port);
        this.mPushClient = mPushClient;
        this.messageDispatcher = new MessageDispatcher(POLICY_LOG);
    }

    @Override
    public void init() {
        super.init();
        messageDispatcher.register(Command.OK, () -> new GatewayOKHandler(mPushClient));
        messageDispatcher.register(Command.ERROR, () -> new GatewayErrorHandler(mPushClient));
        channelHandler = new UDPChannelHandler(messageDispatcher);
        channelHandler.setMulticastAddress(Utils.getInetAddress(CC.mp.net.gateway_client_multicast));
        channelHandler.setNetworkInterface(Utils.getLocalNetworkInterface());
    }


    @Override
    public void stop(Listener listener) {
        super.stop(listener);
    }


    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
        b.option(ChannelOption.IP_MULTICAST_TTL, 255);
        if (snd_buf.gateway_client > 0) b.option(ChannelOption.SO_SNDBUF, snd_buf.gateway_client);
        if (rcv_buf.gateway_client > 0) b.option(ChannelOption.SO_RCVBUF, rcv_buf.gateway_client);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public Connection getConnection() {
        return channelHandler.getConnection();
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
