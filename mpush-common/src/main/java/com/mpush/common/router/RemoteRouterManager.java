package com.mpush.common.router;

import com.mpush.cache.redis.RedisKey;
import com.mpush.api.router.RouterManager;
import com.mpush.cache.redis.manager.RedisManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(RemoteRouterManager.class);

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
    	LOGGER.info("register remote router success userId={}, router={}", userId, router);
    	String key = RedisKey.getUserKey(userId);
        RemoteRouter old = RedisManager.I.get(key, RemoteRouter.class);
        if (old != null) {
            RedisManager.I.del(key);
        }
        RedisManager.I.set(key, router);
        return old;
    }

    @Override
    public boolean unRegister(String userId) {
    	String key = RedisKey.getUserKey(userId);
        RedisManager.I.del(key);
        LOGGER.info("unRegister remote router success userId={}", userId);
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
    	String key = RedisKey.getUserKey(userId);
        return RedisManager.I.get(key, RemoteRouter.class);
    }
}
