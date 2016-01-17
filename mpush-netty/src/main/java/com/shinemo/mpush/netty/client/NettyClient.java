package com.shinemo.mpush.netty.client;

import java.net.InetSocketAddress;

import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Client;

public  class NettyClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private final ChannelHandler handler;
    private final String host;
    private final int port;
    private Channel channel;

    public NettyClient(final String host, final int port, ChannelHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void init() {
        this.stop();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)//
                .option(ChannelOption.TCP_NODELAY, true)//
                .option(ChannelOption.SO_REUSEADDR, true)//
                .option(ChannelOption.SO_KEEPALIVE, true)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() { // (4)
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new PacketDecoder());
                ch.pipeline().addLast(PacketEncoder.INSTANCE);
                ch.pipeline().addLast(handler);
            }
        });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        if (future.awaitUninterruptibly(4000) && future.isSuccess() && future.channel().isActive()) {
            channel = future.channel();
        } else {
            future.cancel(true);
            future.channel().close();
            LOGGER.warn("[remoting] failure to connect:" + host + "," + port);
        }
    }

    @Override
    public void start() {
        if (channel != null) {
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }

    @Override
    public String getUri() {
        return host + ":" + port;
    }

    @Override
    public ChannelHandler getHandler() {
        return handler;
    }
}
