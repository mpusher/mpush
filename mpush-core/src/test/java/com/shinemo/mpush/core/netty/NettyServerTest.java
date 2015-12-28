package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.Receiver;
import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.core.MessageDispatcher;
import com.shinemo.mpush.core.handler.ServerHandler;
import com.shinemo.mpush.netty.server.NettyServer;
import org.junit.Test;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyServerTest {

    @Test
    public void testStop() throws Exception {

    }

    @Test
    public void testStart() throws Exception {

        Receiver receiver = new MessageDispatcher();
        Handler handler = new ServerHandler(receiver);

        final NettyServer server = new NettyServer(3000, handler);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    server.stop();
                } catch (Exception e) {
                }
            }
        });
    }
}