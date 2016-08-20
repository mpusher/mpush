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
import com.mpush.client.connect.ClientConfig;
import com.mpush.client.connect.ConnectClient;
import com.mpush.common.security.CipherBox;
import com.mpush.tools.log.Logs;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ConnClientTestMain2 {

    public static void main(String[] args) throws Exception {
        Logs.init();
        ConnectClientBoot main = new ConnectClientBoot();
        main.run();

        List<ZKServerNode> serverList = main.getServers();

        int index = (int) ((Math.random() % serverList.size()) * serverList.size());
        ZKServerNode server = serverList.get(index);
        //server = new ZKServerNode("127.0.0.1", 3000, "127.0.0.1", null);

        ClientConfig config = new ClientConfig();
        config.setClientKey(CipherBox.I.randomAESKey());
        config.setIv(CipherBox.I.randomAESIV());
        config.setClientVersion("1.0.0");
        config.setDeviceId("android-device-id-1");
        config.setOsName("android");
        config.setOsVersion("1.0.1");
        config.setUserId("user-0");
        Client client = new ConnectClient(server.getExtranetIp(), server.getPort(), config);
        client.start().get(10, TimeUnit.SECONDS);

        config = new ClientConfig();
        config.setClientKey(CipherBox.I.randomAESKey());
        config.setIv(CipherBox.I.randomAESIV());
        config.setClientVersion("1.0.0");
        config.setDeviceId("pc-device-id-2");
        config.setOsName("pc");
        config.setOsVersion("1.0.1");
        config.setUserId("user-0");
        client = new ConnectClient(server.getExtranetIp(), server.getPort(), config);
        client.start().get(10, TimeUnit.SECONDS);

        LockSupport.park();
    }

}
