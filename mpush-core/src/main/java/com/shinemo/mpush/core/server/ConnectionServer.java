package com.shinemo.mpush.core.server;


import com.shinemo.mpush.api.connection.ConnectionManager;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.common.MessageDispatcher;
import com.shinemo.mpush.core.handler.*;
import com.shinemo.mpush.netty.client.HttpClient;
import com.shinemo.mpush.netty.client.NettyHttpClient;
import com.shinemo.mpush.netty.connection.NettyConnectionManager;
import com.shinemo.mpush.netty.server.NettyServer;
import com.shinemo.mpush.tools.config.ConfigCenter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

/**
 * Created by ohun on 2015/12/30.
 */
public final class ConnectionServer extends NettyServer {
    private ServerChannelHandler channelHandler;

    private ConnectionManager connectionManager = new NettyConnectionManager();
    private HttpClient httpClient;

    public ConnectionServer(int port) {
        super(port);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.HEARTBEAT, new HeartBeatHandler());
        receiver.register(Command.HANDSHAKE, new HandshakeHandler());
        receiver.register(Command.BIND, new BindUserHandler());
        receiver.register(Command.UNBIND, new UnbindUserHandler());
        receiver.register(Command.FAST_CONNECT, new FastConnectHandler());

        if (ConfigCenter.holder.httpProxyEnable()) {
            httpClient = new NettyHttpClient();
            receiver.register(Command.HTTP_PROXY, new HttpProxyHandler(httpClient));
        }
        channelHandler = new ServerChannelHandler(true, connectionManager, receiver);
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        if (httpClient != null) httpClient.stop();
        connectionManager.destroy();
    }

    @Override
    protected void initOptions(ServerBootstrap b) {
        super.initOptions(b);
        /***
         * 你可以设置这里指定的通道实现的配置参数。
         * 我们正在写一个TCP/IP的服务端，
         * 因此我们被允许设置socket的参数选项比如tcpNoDelay和keepAlive。
         * 请参考ChannelOption和详细的ChannelConfig实现的接口文档以此可以对ChannelOptions的有一个大概的认识。
         */
        b.option(ChannelOption.SO_BACKLOG, 1024);

        /**
         * TCP层面的接收和发送缓冲区大小设置，
         * 在Netty中分别对应ChannelOption的SO_SNDBUF和SO_RCVBUF，
         * 需要根据推送消息的大小，合理设置，对于海量长连接，通常32K是个不错的选择。
         */
        b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
        b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }
}
