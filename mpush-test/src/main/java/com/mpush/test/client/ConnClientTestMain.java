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

import com.mpush.client.connect.ClientConfig;
import com.mpush.client.connect.ConnClientChannelHandler;
import com.mpush.common.security.CipherBox;
import com.mpush.tools.log.Logs;
import com.mpush.zk.node.ZKServerNode;
import io.netty.channel.ChannelFuture;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ConnClientTestMain {

    public static void main(String[] args) throws Exception {
        int count = 1, printDelay = 1;
        boolean sync = true;
        if (args.length > 0) {
            count = NumberUtils.toInt(args[0], count);
        }
        if (args.length > 1) {
            printDelay = NumberUtils.toInt(args[1], printDelay);
        }

        if (args.length > 2) {
            sync = !"1".equals(args[2]);
        }

        testConnClient(count, printDelay, sync);
    }

    @Test
    public void testConnClient() throws Exception {
        testConnClient(1, 1, true);
        LockSupport.park();
    }

    private static void testConnClient(int count, int printDelay, boolean sync) throws Exception {
        Logs.init();
        ConnClientBoot boot = new ConnClientBoot();
        boot.start().get();
        List<ZKServerNode> serverList = boot.getServers();
        if (serverList.isEmpty()) {
            boot.stop();
            System.out.println("no mpush server.");
            return;
        }

        Executors
                .newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> System.err.println(ConnClientChannelHandler.STATISTICS)
                        , 3, printDelay, TimeUnit.SECONDS
                );

        for (int i = 0; i < count; i++) {
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

            int L = serverList.size();
            int index = (int) ((Math.random() % L) * L);
            ZKServerNode server = serverList.get(index);

            ChannelFuture future = boot.connect(server.getExtranetIp(), server.getPort(), config);
            if (sync) future.awaitUninterruptibly();
        }
    }
}
