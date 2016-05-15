package com.mpush.core.netty;

import com.mpush.api.Server;
import com.mpush.core.server.ConnectionServer;
import org.junit.Test;

/**
 * Created by ohun on 2015/12/24.
 *
 * @author ohun@live.cn
 */
public class NettyServerTest {
    @Test
    public void testStart() throws Exception {
        ConnectionServer server = new ConnectionServer(3000);
        server.init();
        server.start(new Server.Listener() {
            @Override
            public void onSuccess(int port) {
            }

            @Override
            public void onFailure(Throwable cause) {
            }
        });
    }
}