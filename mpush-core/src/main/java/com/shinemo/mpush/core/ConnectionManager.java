package com.shinemo.mpush.core;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.shinemo.mpush.api.Connection;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionManager {
    public static final ConnectionManager INSTANCE = new ConnectionManager();
//    private final ConcurrentMap<String, NettyConnection> connections = new ConcurrentHashMapV8<String, NettyConnection>();

	private final Cache<String,NettyConnection> cacherClients = CacheBuilder.newBuilder()
		      .maximumSize(2<<17)
		      .expireAfterAccess(27, TimeUnit.MINUTES)
		      .removalListener(new RemovalListener<String, NettyConnection>() {
		    	  public void onRemoval(RemovalNotification<String,NettyConnection> notification) {
		    		  if(notification.getValue().isClosed()){
//		    			  notification.getValue().close("[Remoting] removed from cache");
		    		  }
		    	  };
			}).build();
    
    public Connection get(final String channelId) throws ExecutionException {
    	
    	NettyConnection client = cacherClients.get(channelId, new Callable<NettyConnection>() {
            @Override
            public NettyConnection call() throws Exception {
            	NettyConnection client = getFromRedis(channelId);
                return client;
            }
        });
        if (client == null || !client.isClosed()) {
            cacherClients.invalidate(channelId);
            return null;
        }
        return client;
    	
    }

    public void add(NettyConnection connection) {
    	cacherClients.put(connection.getId(), connection);
    }

    public void remove(String channelId) {
    	cacherClients.invalidate(channelId);
    }
    
    private NettyConnection getFromRedis(String channelId){
    	return null;
    }
}
