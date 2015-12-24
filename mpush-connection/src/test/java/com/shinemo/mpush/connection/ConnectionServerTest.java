package com.shinemo.mpush.connection;

import org.junit.Test;

import com.shinemo.mpush.connection.netty.server.ConnectionServer;

/**
 * Created by ohun on 2015/12/24.
 */
public class ConnectionServerTest {

	@Test
    public void testStop() throws Exception {

    }

	@Test
    public void testStart() throws Exception {
        ConnectionServer server = new ConnectionServer(3000);
        server.start();
    }
}