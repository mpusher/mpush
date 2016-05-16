package com.mpush.test.connection.mpns;

import com.mpush.common.security.CipherBox;
import com.mpush.conn.client.ClientChannelHandler;
import com.mpush.netty.client.NettyClientFactory;
import com.mpush.netty.client.SecurityNettyClient;
import com.mpush.zk.ZKServerNode;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ConnectTestClient main = new ConnectTestClient();
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
            byte[] clientKey = CipherBox.INSTANCE.randomAESKey();
            byte[] iv = CipherBox.INSTANCE.randomAESIV();

            SecurityNettyClient client = new SecurityNettyClient(server.getIp(), server.getPort());
            client.setClientKey(clientKey);
            client.setIv(iv);
            client.setClientVersion(clientVersion);
            client.setDeviceId(deviceId);
            client.setOsName(osName);
            client.setOsVersion(osVersion);
            client.setUserId(userId);
            client.setCipher(cipher);

            ClientChannelHandler handler = new ClientChannelHandler(client);
            NettyClientFactory.INSTANCE.create(handler);
            Thread.sleep(100);
        }

        LockSupport.park();
    }

}
