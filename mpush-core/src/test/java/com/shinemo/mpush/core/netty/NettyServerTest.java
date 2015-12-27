package com.shinemo.mpush.core.netty;

import org.junit.Test;

import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.core.MessageReceiver;
import com.shinemo.mpush.core.handler.ServerHandler;
import com.shinemo.mpush.netty.server.NettyServer;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyServerTest {

	@Test
    public void testStop() throws Exception {

    }

	@Test
    public void testStart() throws Exception {
		
		MessageReceiver receiver = new MessageReceiver();
		Handler handler = new ServerHandler(receiver);
		
        final NettyServer server = new NettyServer(3000,handler);
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