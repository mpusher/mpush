package com.shinemo.mpush.netty.client;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.netty.codec.PacketDecoder;
import com.shinemo.mpush.netty.codec.PacketEncoder;
import com.shinemo.mpush.netty.util.NettySharedHolder;

public class NettyClientFactory {
	
	private static final Logger log = LoggerFactory.getLogger(NettyClientFactory.class);

    public static final NettyClientFactory INSTANCE = new NettyClientFactory();
    
    private final Map<Channel, Client> channel2Client = Maps.newConcurrentMap();
    
    public Client create(final ChannelClientHandler handler){
    	 final Bootstrap bootstrap = new Bootstrap();
         bootstrap.group(NettySharedHolder.workerGroup)//
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
         
         Client client = handler.getClient();
         
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(client.getHost(), client.getPort()));
         if (future.awaitUninterruptibly(4000) && future.isSuccess() && future.channel().isActive()) {
             Channel channel = future.channel();
             log.error("init channel:"+channel);
             return client;
         } else {
             future.cancel(true);
             future.channel().close();
             log.warn("[remoting] failure to connect:" + client);
             return null;
         }
    }
    
    @Deprecated
    public Client create(String host,int port,final ChannelHandler handler){
    	Client client = new NettyClient(host, port);
    	return init(client, handler);
    }
    
    @Deprecated
    public Client createSecurityClient(String host,int port,final ChannelHandler handler,byte[] clientKey,byte[] iv,String clientVersion,
    		                           String deviceId,String osName,String osVersion,String userId,String cipher){
    	SecurityNettyClient client = new SecurityNettyClient(host, port);
    	client.setClientKey(clientKey);
    	client.setIv(iv);
    	client.setClientVersion(clientVersion);
    	client.setDeviceId(deviceId);
    	client.setOsName(osName);
    	client.setOsVersion(osVersion);
    	client.setUserId(userId);
    	client.setCipher(cipher);
    	return init(client, handler);
    }

    public Client init(Client client, final ChannelHandler handler) {
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(NettySharedHolder.workerGroup)//
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
        
        
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(client.getHost(), client.getPort()));
        if (future.awaitUninterruptibly(4000) && future.isSuccess() && future.channel().isActive()) {
            Channel channel = future.channel();
            log.error("init channel:"+channel);
            return client;
        } else {
            future.cancel(true);
            future.channel().close();
            log.warn("[remoting] failure to connect:" + client);
            return null;
        }
    }

    public Client getCientByChannel(final Channel channel) {
        return channel2Client.get(channel);
    }

    public void remove(final Channel channel) {
        channel2Client.remove(channel);
    }
    
    public void put(Channel channel,final Client client){
    	channel2Client.put(channel, client);
    }
    
}
