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

package com.mpush.test.zk;

import com.mpush.api.spi.common.ServiceDiscoveryFactory;
import com.mpush.api.spi.common.ServiceRegistryFactory;
import com.mpush.api.srd.*;
import com.mpush.common.ServerNodes;
import com.mpush.zk.ZKClient;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

import static org.apache.curator.utils.ZKPaths.PATH_SEPARATOR;

/**
 * Created by ohun on 2016/12/27.
 *
 * @author ohun@live.cn (夜色)
 */
public final class ZKClientTest {
    public static void main(String[] args) {
        ServiceRegistry registry = ServiceRegistryFactory.create();
        registry.syncStart();

        registry.register(ServerNodes.gs());
        registry.register(ServerNodes.gs());
        registry.deregister(ServerNodes.gs());
        LockSupport.park();
    }

    @Test
    public void testDiscovery() {
        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.syncStart();

        System.err.println(discovery.lookup(ServiceNames.GATEWAY_SERVER));
        discovery.subscribe(ServiceNames.GATEWAY_SERVER, new ServiceListener() {
            @Override
            public void onServiceAdded(String path, ServiceNode node) {
                System.err.println(path + "," + node);
                System.err.println(discovery.lookup(ServiceNames.GATEWAY_SERVER));
            }

            @Override
            public void onServiceUpdated(String path, ServiceNode node) {
                System.err.println(path + "," + node);
                System.err.println(discovery.lookup(ServiceNames.GATEWAY_SERVER));
            }

            @Override
            public void onServiceRemoved(String path, ServiceNode node) {
                System.err.println(path + "," + node);
                System.err.println(discovery.lookup(ServiceNames.GATEWAY_SERVER));
            }
        });
        LockSupport.park();
    }

    @Test
    public void testZK() throws Exception {
        ZKClient.I.syncStart();
        ZKClient.I.registerEphemeral(ServerNodes.gs().serviceName(), "3");
        ZKClient.I.registerEphemeral(ServerNodes.gs().serviceName(), "4");
        System.err.println("==================" + ZKClient.I.getChildrenKeys(ServiceNames.GATEWAY_SERVER));
        List<String> rawData = ZKClient.I.getChildrenKeys(ServiceNames.GATEWAY_SERVER);
        if (rawData == null || rawData.isEmpty()) {
            return;
        }
        for (String raw : rawData) {
            String fullPath = ServiceNames.GATEWAY_SERVER + PATH_SEPARATOR + raw;
            System.err.println("==================" + ZKClient.I.get(fullPath));
        }

    }
}
