package com.shinemo.mpush.common.router;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2016/1/4.
 */
public class ConnectionRouterManager extends RemoteRouterManager {
    public static final ConnectionRouterManager INSTANCE = new ConnectionRouterManager();
    // TODO: 2015/12/30 可以增加一层本地缓存，防止疯狂查询redis, 但是要注意失效问题及数据不一致问题
    private final Cache<String, RemoteRouter> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();


    @Override
    public RemoteRouter register(String userId, RemoteRouter route) {
        RemoteRouter old = cache.getIfPresent(userId);
        cache.put(userId, route);
        return old;
    }

    @Override
    public boolean unRegister(String userId) {
        cache.invalidate(userId);
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
        return cache.getIfPresent(userId);
    }

    /**
     * 如果推送失败，可能是缓存不一致了，可以让本地缓存失效
     * <p>
     * 失效对应的本地缓存
     *
     * @param userId
     */
    public void invalidateLocalCache(String userId) {
        cache.invalidate(userId);
    }
}
