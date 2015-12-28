package com.shinemo.mpush.core.netty;


import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.netty.client.NettyClientFactory;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyClientTest {

    private static final Logger log = LoggerFactory.getLogger(NettyClientTest.class);
    
    private String host = "127.0.0.1";
    private int port = 3000;
    private Handler handler = new ClientHandler();
    
    @Test
    public void testClient() throws Exception {
    	
    	Client client = NettyClientFactory.instance.get(host, port, handler);
    	
    	
    	LockSupport.park();
    	
    	client.close("");
    	
    	log.error(ToStringBuilder.reflectionToString(client, ToStringStyle.MULTI_LINE_STYLE));
    	
    }

}
