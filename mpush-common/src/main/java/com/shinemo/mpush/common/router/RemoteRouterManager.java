package com.shinemo.mpush.common.router;

import com.shinemo.mpush.api.router.RouterManager;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(RemoteRouterManager.class);

    @Override
    public RemoteRouter register(String userId, RemoteRouter route) {
        RemoteRouter old = RedisManage.get(userId, RemoteRouter.class);
        if (old != null) {
            RedisManage.del(userId);
        }
        RedisManage.set(userId, route);
        return old;
    }

    @Override
    public boolean unRegister(String userId) {
        RedisManage.del(userId);
        LOGGER.info("unRegister local router success userId={}", userId);
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
        return RedisManage.get(userId, RemoteRouter.class);
    }
}
