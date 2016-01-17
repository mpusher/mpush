package com.shinemo.mpush.netty.client;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

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

    public Client createGet(String remoteHost, int port, ChannelHandler handler) {
        Client client = createClient(remoteHost, port, handler);
        if (client != null) {
            String key = String.format(format, remoteHost, port);
            cachedClients.put(key, client);
        }
        return client;
    }

    abstract Client createClient(String remoteHost, int port, ChannelHandler handler);

    public void remove(Client client) {
        if (client != null) {
            cachedClients.invalidate(client.getUri());
            LOGGER.warn(MessageFormat.format("[Remoting] {0} is removed", client));
        }
    }
}
