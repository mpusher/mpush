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

package com.mpush.test.client;

import com.mpush.api.service.Client;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.client.connect.ClientConfig;
import com.mpush.client.connect.ConnClientChannelHandler;
import com.mpush.client.connect.ConnectClient;
import com.mpush.common.security.CipherBox;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKServerNodeWatcher;
import com.mpush.zk.node.ZKServerNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ConnClientTestMain {

    public static void main(String[] args) throws Exception {
        new ConnClientTestMain().testConnClient();
    }

    @Test
    public void testConnClient() throws Exception {
        Logs.init();
        ConnClientBoot boot = new ConnClientBoot();
        boot.start().get();
        List<ZKServerNode> serverList = boot.getServers();


        for (int i = 0; i < 1000; i++) {
            String clientVersion = "1.0." + i;
            String osName = "android";
            String osVersion = "1.0.1";
            String userId = "user-" + i;
            String deviceId = "test-device-id-" + i;
            byte[] clientKey = CipherBox.I.randomAESKey();
            byte[] iv = CipherBox.I.randomAESIV();

            ClientConfig config = new ClientConfig();
            config.setClientKey(clientKey);
            config.setIv(iv);
            config.setClientVersion(clientVersion);
            config.setDeviceId(deviceId);
            config.setOsName(osName);
            config.setOsVersion(osVersion);
            config.setUserId(userId);

            int index = (int) ((Math.random() % serverList.size()) * serverList.size());
            ZKServerNode server = serverList.get(index);
            boot.connect(server.getExtranetIp(), server.getPort(), config);
            System.out.println("client num=" + i);
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.err.println(ConnClientChannelHandler.STATISTICS);
        }, 0, 1, TimeUnit.SECONDS);

        LockSupport.park();
    }
}
