package com.mpush.test.client;

import com.mpush.api.Client;
import com.mpush.client.connect.ClientConfig;
import com.mpush.client.connect.ConnectClient;
import com.mpush.common.security.CipherBox;
import com.mpush.tools.log.Logs;
import com.mpush.zk.node.ZKServerNode;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class ConnClientTestMain {

    public static void main(String[] args) throws Exception {
        Logs.init();
        ConnectClientBoot main = new ConnectClientBoot();
        main.start();

        List<ZKServerNode> serverList = main.getServers();

        int index = (int) ((Math.random() % serverList.size()) * serverList.size());
        ZKServerNode server = serverList.get(index);

        for (int i = 0; i < 1; i++) {
            String clientVersion = "1.0." + i;
            String osName = "android";
            String osVersion = "1.0.1";
            String userId = "user-" + i;
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
            client.start(null);
            Thread.sleep(10);
        }

        LockSupport.park();

    }

}
