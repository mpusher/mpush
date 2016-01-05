package com.shinemo.mpush.common.router;

import com.shinemo.mpush.api.router.RouterManager;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

/**
 * Created by ohun on 2015/12/23.
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {

    @Override
    public RemoteRouter register(String userId, RemoteRouter route) {
        RemoteRouter old = RedisManage.get(userId, RemoteRouter.class);
        RedisManage.set(userId, route);
        return old;
    }

    @Override
    public boolean unRegister(String userId) {
        RedisManage.del(userId);
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
        return RedisManage.get(userId, RemoteRouter.class);
    }
}
