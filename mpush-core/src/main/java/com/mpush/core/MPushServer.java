/*
 * (C) Copyright 2015-2017 the original author or authors.
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

package com.mpush.core;

import com.mpush.api.MPushContext;
import com.mpush.api.common.Monitor;
import com.mpush.api.spi.common.*;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceNode;
import com.mpush.api.srd.ServiceRegistry;
import com.mpush.common.ServerNodes;
import com.mpush.common.user.UserManager;
import com.mpush.core.ack.AckTaskQueue;
import com.mpush.core.push.PushCenter;
import com.mpush.core.router.RouterCenter;
import com.mpush.core.server.*;
import com.mpush.core.session.ReusableSessionManager;
import com.mpush.monitor.service.MonitorService;
import com.mpush.netty.http.HttpClient;
import com.mpush.netty.http.NettyHttpClient;
import com.mpush.tools.event.EventBus;
import com.mpush.monitor.service.ThreadPoolManager;

import static com.mpush.tools.config.CC.mp.net.tcpGateway;

/**
 * Created by ohun on 2017/6/14.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MPushServer implements MPushContext {

    private ServiceNode connServerNode;
    private ServiceNode gatewayServerNode;
    private ServiceNode websocketServerNode;

    private ConnectionServer connectionServer;
    private WebsocketServer websocketServer;
    private GatewayServer gatewayServer;
    private AdminServer adminServer;
    private GatewayUDPConnector udpGatewayServer;

    private HttpClient httpClient;

    private PushCenter pushCenter;

    private ReusableSessionManager reusableSessionManager;

    private RouterCenter routerCenter;

    private MonitorService monitorService;


    public MPushServer() {
        connServerNode = ServerNodes.cs();
        gatewayServerNode = ServerNodes.gs();
        websocketServerNode = ServerNodes.ws();

        monitorService = new MonitorService();
        EventBus.create(monitorService.getThreadPoolManager().getEventBusExecutor());

        reusableSessionManager = new ReusableSessionManager();

        pushCenter = new PushCenter(this);

        routerCenter = new RouterCenter(this);

        connectionServer = new ConnectionServer(this);

        websocketServer = new WebsocketServer(this);

        adminServer = new AdminServer(this);

        if (tcpGateway()) {
            gatewayServer = new GatewayServer(this);
        } else {
            udpGatewayServer = new GatewayUDPConnector(this);
        }
    }

    public boolean isTargetMachine(String host, int port) {
        return port == gatewayServerNode.getPort() && gatewayServerNode.getHost().equals(host);
    }

    public ServiceNode getConnServerNode() {
        return connServerNode;
    }

    public ServiceNode getGatewayServerNode() {
        return gatewayServerNode;
    }

    public ServiceNode getWebsocketServerNode() {
        return websocketServerNode;
    }

    public ConnectionServer getConnectionServer() {
        return connectionServer;
    }

    public GatewayServer getGatewayServer() {
        return gatewayServer;
    }

    public AdminServer getAdminServer() {
        return adminServer;
    }

    public GatewayUDPConnector getUdpGatewayServer() {
        return udpGatewayServer;
    }

    public WebsocketServer getWebsocketServer() {
        return websocketServer;
    }

    public HttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (this) {
                if (httpClient == null) {
                    httpClient = new NettyHttpClient();
                }
            }
        }
        return httpClient;
    }

    public PushCenter getPushCenter() {
        return pushCenter;
    }

    public ReusableSessionManager getReusableSessionManager() {
        return reusableSessionManager;
    }

    public RouterCenter getRouterCenter() {
        return routerCenter;
    }

    @Override
    public MonitorService getMonitor() {
        return monitorService;
    }

    @Override
    public ServiceDiscovery getDiscovery() {
        return ServiceDiscoveryFactory.create();
    }

    @Override
    public ServiceRegistry getRegistry() {
        return ServiceRegistryFactory.create();
    }

    @Override
    public CacheManager getCacheManager() {
        return CacheManagerFactory.create();
    }

    @Override
    public MQClient getMQClient() {
        return MQClientFactory.create();
    }
}
