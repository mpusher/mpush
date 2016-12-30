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


import com.mpush.bootstrap.job.*;
import com.mpush.core.server.*;

import static com.mpush.common.ServerNodes.*;
import static com.mpush.tools.config.CC.mp.net.tcpGateway;
import static com.mpush.tools.config.CC.mp.net.udpGateway;
import static com.mpush.tools.config.CC.mp.net.wsEnabled;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public final class ServerLauncher {

    private final BootChain chain = BootChain.chain();

    public ServerLauncher() {
        chain.boot()
                .setNext(new CacheManagerBoot())//1.初始化缓存模块
                .setNext(new ServiceRegistryBoot())//2.启动服务注册与发现模块
                .setNext(new ServerBoot(ConnectionServer.I(), CS))//3.启动接入服务
                .setNext(() -> new ServerBoot(WebSocketServer.I(), WS), wsEnabled())//4.启动websocket接入服务
                .setNext(() -> new ServerBoot(GatewayUDPConnector.I(), GS), udpGateway())//5.启动udp网关服务
                .setNext(() -> new ServerBoot(GatewayServer.I(), GS), tcpGateway())//6.启动tcp网关服务
                .setNext(new ServerBoot(AdminServer.I(), null))//7.启动控制台服务
                .setNext(new PushCenterBoot())//8.启动推送中心组件
                .setNext(new HttpProxyBoot())//9.启动http代理服务，dns解析服务
                .setNext(new MonitorBoot())//10.启动监控服务
                .end();
    }

    public void start() {
        chain.start();
    }

    public void stop() {
        chain.stop();
    }
}
