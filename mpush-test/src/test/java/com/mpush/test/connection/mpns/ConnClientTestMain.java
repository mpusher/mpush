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

package com.mpush.test.connection.mpns;

import com.mpush.api.Client;
import com.mpush.client.connect.ClientConfig;
import com.mpush.client.connect.ConnectClient;
import com.mpush.common.security.CipherBox;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class ConnClientTestMain {

    public static void main(String[] args) throws InterruptedException {

        ConnectTestClientBoot main = new ConnectTestClientBoot();
        main.start();

        List<ZKServerNode> serverList = main.getServers();

        int index = (int) ((Math.random() % serverList.size()) * serverList.size());
        ZKServerNode server = serverList.get(index);

        for (int i = 0; i < 1000; i++) {
            String clientVersion = "1.0." + i;
            String osName = "android";
            String osVersion = "1.0.1";
            String userId = "uh-" + i;
            String deviceId = "test-device-id-" + i;
            String cipher = "";
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
            config.setCipher(cipher);
            Client client = new ConnectClient(server.getIp(), server.getPort(), config);
            Thread.sleep(100);
        }

        LockSupport.park();
    }

}
