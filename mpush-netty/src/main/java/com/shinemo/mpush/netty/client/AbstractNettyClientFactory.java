package com.shinemo.mpush.netty.client;

import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.shinemo.mpush.api.Client;

public abstract class AbstractNettyClientFactory {

    private static final String format = "%s:%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNettyClientFactory.class);

    /**
     * host:port
     */
    private final Cache<String, Client> cachedClients = CacheBuilder.newBuilder()//
            .maximumSize(2 << 17)// 最大是65535*2
            .expireAfterAccess(2 * 60, TimeUnit.MINUTES)// 如果经过120分钟没有访问，释放掉连接，缓解内存和服务器连接压力
            .removalListener(new RemovalListener<String, Client>() {
                @Override
                public void onRemoval(RemovalNotification<String, Client> notification) {
                    if (notification.getValue().isConnected()) {
                        notification.getValue().stop();
                        LOGGER.warn("[Remoting] removed from cache");
                    }
                }
            })
            .build();
    
    /**
     * host:port
     */
    private final Cache<Channel, Client> channelClients = CacheBuilder.newBuilder()//
            .maximumSize(2 << 17)// 最大是65535*2
            .expireAfterAccess(2 * 60, TimeUnit.MINUTES)// 如果经过120分钟没有访问，释放掉连接，缓解内存和服务器连接压力
            .removalListener(new RemovalListener<Channel, Client>() {
                @Override
                public void onRemoval(RemovalNotification<Channel, Client> notification) {
                    if (notification.getValue().isConnected()) {
                        notification.getValue().stop();
                        LOGGER.warn("[Remoting] removed from cache");
                    }
                }
            })
            .build();

    /**
     * 不存在，则创建
     *
     * @param remoteHost
     * @param port
     * @return
     * @throws Exception
     */
    public Client get(final String remoteHost, final int port) {
        String key = String.format(format, remoteHost, port);
        return cachedClients.getIfPresent(key);
    }
    
    public Client get(final Channel channel){
    	return channelClients.getIfPresent(channel);
    }
    
    public Client get(final String remoteHost,final int port,final ChannelHandler handler,final boolean security) throws Exception{
    	String key = String.format(format, remoteHost, port);
    	Client client = cachedClients.get(key, new Callable<Client>() {
    		@Override
    		public Client call() throws Exception {
    			Client client = createClient(remoteHost, port, handler,security);
    			return client;
    		}
		});
    	if(client == null || !client.isConnected()){
    		cachedClients.invalidate(key);
    		return null;
    	}
    	return client;
    }


    abstract Client createClient(String remoteHost, int port, ChannelHandler handler,boolean security);

    public void remove(Client client) {
        if (client != null) {
            cachedClients.invalidate(client.getUrl());
            channelClients.invalidate(client.getChannel());
            LOGGER.warn(MessageFormat.format("[Remoting] {0} is removed", client));
        }
    }
}
