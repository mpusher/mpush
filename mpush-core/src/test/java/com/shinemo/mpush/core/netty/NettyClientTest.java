package com.shinemo.mpush.core.netty;


import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.tools.ConfigCenter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientTest.class);

    @Before
    public void setUp() throws Exception {
        ConfigCenter.INSTANCE.init();
    }

    private String host = "127.0.0.1";
    private int port = ConfigCenter.INSTANCE.getConnectionServerPort();
    private ClientChannelHandler handler = new ClientChannelHandler();

    @Test
    public void testClient() throws Exception {
        Client client = NettyClientFactory.INSTANCE.get(host, port, handler);
        client.init();
        client.start();
    }

}
