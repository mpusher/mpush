package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.Server;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.netty.server.NettyServer;
import com.shinemo.mpush.tools.ConfigCenter;
import org.junit.Test;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyServerTest {
    @Test
    public void testStart() throws Exception {
        ConfigCenter.INSTANCE.init();
        ConnectionServer server = new ConnectionServer(3000);
        server.init();
        server.start(new Server.Listener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String message) {

            }
        });
    }
}