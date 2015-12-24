package com.shinemo.mpush.connection;

/**
 * Created by ohun on 2015/12/24.
 */
public class ConnectionServerTest {

    @org.junit.Test
    public void testStop() throws Exception {

    }

    @org.junit.Test
    public void testStart() throws Exception {
        ConnectionServer server = new ConnectionServer(3000);
        server.start();
    }
}