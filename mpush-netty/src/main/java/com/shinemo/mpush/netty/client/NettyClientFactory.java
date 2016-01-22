package com.shinemo.mpush.netty.client;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;

public class NettyClientFactory {
	
	private static final Logger log = LoggerFactory.getLogger(NettyClientFactory.class);

    public static final NettyClientFactory INSTANCE = new NettyClientFactory();
    
    private final Map<Channel, Client> channel2Client = Maps.newConcurrentMap();

    public Client createClient(String host, int port, final ChannelHandler handler,boolean security) {
        final Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap.group(workerGroup)//
        		.option(ChannelOption.TCP_NODELAY, true)//
                .option(ChannelOption.SO_REUSEADDR, true)//
                .option(ChannelOption.SO_KEEPALIVE, true)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                .channel(NioSocketChannel.class)
                .handler(handler)
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
            
            Client client = null;
            if(security){
				client = new SecurityNettyClient(host,port, channel);
			}else{
				client = new NettyClient(host,port, channel);
			}
            channel2Client.put(channel, client);
            return client;
        } else {
            future.cancel(true);
            future.channel().close();
            log.warn("[remoting] failure to connect:" + host+","+port);
            return null;
        }
    }

    public Client getCientByChannel(final Channel channel) {
        return channel2Client.get(channel);
    }

    public void remove(final Channel channel) {
        channel2Client.remove(channel);
    }
    
}
