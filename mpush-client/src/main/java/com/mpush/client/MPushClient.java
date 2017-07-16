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

package com.mpush.client;

import com.mpush.api.MPushContext;
import com.mpush.api.spi.common.*;
import com.mpush.api.srd.ServiceDiscovery;
import com.mpush.api.srd.ServiceRegistry;
import com.mpush.client.gateway.connection.GatewayConnectionFactory;
import com.mpush.client.push.PushRequestBus;
import com.mpush.common.router.CachedRemoteRouterManager;
import com.mpush.monitor.service.MonitorService;
import com.mpush.monitor.service.ThreadPoolManager;
import com.mpush.tools.event.EventBus;

/**
 * Created by ohun on 2017/7/15.
 *
 * @author ohun@live.cn (夜色)
 */
public final class MPushClient implements MPushContext {

    private MonitorService monitorService;

    private PushRequestBus pushRequestBus;

    private CachedRemoteRouterManager cachedRemoteRouterManager;

    private GatewayConnectionFactory gatewayConnectionFactory;

    public MPushClient() {
        monitorService = new MonitorService();

        EventBus.create(monitorService.getThreadPoolManager().getEventBusExecutor());

        pushRequestBus = new PushRequestBus(this);

        cachedRemoteRouterManager = new CachedRemoteRouterManager();

        gatewayConnectionFactory = GatewayConnectionFactory.create(this);
    }

    public MonitorService getMonitorService() {
        return monitorService;
    }

    public ThreadPoolManager getThreadPoolManager() {
        return monitorService.getThreadPoolManager();
    }

    public PushRequestBus getPushRequestBus() {
        return pushRequestBus;
    }

    public CachedRemoteRouterManager getCachedRemoteRouterManager() {
        return cachedRemoteRouterManager;
    }

    public GatewayConnectionFactory getGatewayConnectionFactory() {
        return gatewayConnectionFactory;
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
