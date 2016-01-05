package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.common.MessageDispatcher;
import com.shinemo.mpush.core.handler.FastConnectHandler;
import com.shinemo.mpush.core.server.ServerChannelHandler;
import com.shinemo.mpush.core.handler.BindUserHandler;
import com.shinemo.mpush.core.handler.HandshakeHandler;
import com.shinemo.mpush.core.handler.HeartBeatHandler;
import com.shinemo.mpush.netty.connection.NettyConnectionManager;
import com.shinemo.mpush.core.server.ConnectionServer;
import com.shinemo.mpush.netty.server.NettyServer;

import com.shinemo.mpush.tools.Jsons;
import io.netty.channel.ChannelHandler;

import org.junit.Test;

/**
 * Created by ohun on 2015/12/24.
 */
public class NettyServerTest {
    byte[] bytes = new byte[]{1, 2, 3};

    @Test
    public void testStop() throws Exception {
        String json = Jsons.toJson(this);
        NettyServerTest test = Jsons.fromJson(json, NettyServerTest.class);
        System.out.println(json);
        System.out.println(test.bytes.length);

    }

    @Test
    public void testStart() throws Exception {

        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.HEARTBEAT, new HeartBeatHandler());
        receiver.register(Command.HANDSHAKE, new HandshakeHandler());
        receiver.register(Command.BIND, new BindUserHandler());
        receiver.register(Command.FAST_CONNECT, new FastConnectHandler());
        NettyConnectionManager connectionManager = new NettyConnectionManager();
        connectionManager.init();
        ChannelHandler handler = new ServerChannelHandler(connectionManager, receiver);

        final NettyServer server = new ConnectionServer(3000, handler);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.stop();
            }
        });
    }
}