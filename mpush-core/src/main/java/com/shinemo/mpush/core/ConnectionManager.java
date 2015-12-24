package com.shinemo.mpush.core;


import com.shinemo.mpush.api.Connection;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by ohun on 2015/12/22.
 */
public class ConnectionManager {
    public static final ConnectionManager INSTANCE = new ConnectionManager();
    
    //可能会有20w的链接数
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMapV8<String, Connection>();

//	private final Cache<String,NettyConnection> cacherClients = CacheBuilder.newBuilder()
//		      .maximumSize(2<<17)
//		      .expireAfterAccess(27, TimeUnit.MINUTES)
//		      .removalListener(new RemovalListener<String, NettyConnection>() {
//		    	  public void onRemoval(RemovalNotification<String,NettyConnection> notification) {
//		    		  if(notification.getValue().isClosed()){
////		    			  notification.getValue().close("[Remoting] removed from cache");
//		    		  }
//		    	  };
//			}).build();
    
    public Connection get(final String channelId) throws ExecutionException {
    	
//    	NettyConnection client = cacherClients.get(channelId, new Callable<NettyConnection>() {
//            @Override
//            public NettyConnection call() throws Exception {
//            	NettyConnection client = getFromRedis(channelId);
//                return client;
//            }
//        });
//        if (client == null || !client.isClosed()) {
//            cacherClients.invalidate(channelId);
//            return null;
//        }
//        return client;
    	
    	return connections.get(channelId);
    	
    }

    public void add(Connection connection) {
    	connections.putIfAbsent(connection.getId(), connection);
    }

    public void remove(Connection connection) {
    	connections.remove(connection.getId());
    }
    
}
