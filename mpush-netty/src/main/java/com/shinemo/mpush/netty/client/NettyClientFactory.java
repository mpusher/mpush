package com.shinemo.mpush.netty.client;

import java.net.InetSocketAddress;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClientFactory extends AbstractNettyClientFactory {

    private static final Logger log = LoggerFactory.getLogger(NettyClientFactory.class);

    public static NettyClientFactory instance = new NettyClientFactory();

    protected Client createClient(final String host, final int port, final ChannelHandler handler) throws Exception {
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
            Channel channel = future.channel();
            Client client = new NettyClient(host, port, channel);
            return client;
        } else {
            future.cancel(true);
            future.channel().close();
            log.warn("[remoting] failure to connect:" + host + "," + port);
        }
        throw new Exception("create client exception");
    }

    public Client getClient(final Client client) throws Exception {
        return get(client.getRemoteHost(), client.getRemotePort());
    }

    public void remove(final Client client) {
        super.remove(client);
    }

    public void close(final Client client) {

    }

}
