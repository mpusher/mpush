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

package com.mpush.bootstrap;


import com.mpush.api.common.ServerEventListener;
import com.mpush.api.spi.core.ServerEventListenerFactory;
import com.mpush.bootstrap.job.*;
import com.mpush.core.MPushServer;
import com.mpush.tools.config.CC;

import static com.mpush.tools.config.CC.mp.net.*;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public final class ServerLauncher {

    private MPushServer mPushServer;
    private BootChain chain;
    private ServerEventListener serverEventListener;

    public void init() {
        if (mPushServer == null) {
            mPushServer = new MPushServer();
        }

        if (chain == null) {
            chain = BootChain.chain();
        }

        if (serverEventListener == null) {
            serverEventListener = ServerEventListenerFactory.create();
        }

        serverEventListener.init(mPushServer);

        chain.boot()
                .setNext(new CacheManagerBoot())//1.初始化缓存模块
                .setNext(new ServiceRegistryBoot())//2.启动服务注册与发现模块
                .setNext(new ServiceDiscoveryBoot())//2.启动服务注册与发现模块
                .setNext(new ServerBoot(mPushServer.getConnectionServer(), mPushServer.getConnServerNode()))//3.启动接入服务
                .setNext(() -> new ServerBoot(mPushServer.getWebsocketServer(), mPushServer.getWebsocketServerNode()), wsEnabled())//4.启动websocket接入服务
                .setNext(() -> new ServerBoot(mPushServer.getUdpGatewayServer(), mPushServer.getGatewayServerNode()), udpGateway())//5.启动udp网关服务
                .setNext(() -> new ServerBoot(mPushServer.getGatewayServer(), mPushServer.getGatewayServerNode()), tcpGateway())//6.启动tcp网关服务
                .setNext(new ServerBoot(mPushServer.getAdminServer(), null))//7.启动控制台服务
                .setNext(new RouterCenterBoot(mPushServer))//8.启动路由中心组件
                .setNext(new PushCenterBoot(mPushServer))//9.启动推送中心组件
                .setNext(() -> new HttpProxyBoot(mPushServer), CC.mp.http.proxy_enabled)//10.启动http代理服务，dns解析服务
                .setNext(new MonitorBoot(mPushServer))//11.启动监控服务
                .end();
    }

    public void start() {
        chain.start();
    }

    public void stop() {
        chain.stop();
    }

    public void setMPushServer(MPushServer mPushServer) {
        this.mPushServer = mPushServer;
    }

    public void setChain(BootChain chain) {
        this.chain = chain;
    }

    public MPushServer getMPushServer() {
        return mPushServer;
    }

    public BootChain getChain() {
        return chain;
    }

    public ServerEventListener getServerEventListener() {
        return serverEventListener;
    }

    public void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }
}
