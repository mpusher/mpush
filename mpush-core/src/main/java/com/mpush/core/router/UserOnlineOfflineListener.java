package com.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.common.user.UserManager;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.event.EventBus;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class UserOnlineOfflineListener {

    public static final String ONLINE_CHANNEL = "/mpush/online/";

    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    public UserOnlineOfflineListener() {
        EventBus.I.register(this);
    }

    @Subscribe
    void onUserOnline(UserOnlineEvent event) {
        UserManager.INSTANCE.recordUserOnline(event.getUserId());
        RedisManager.I.publish(ONLINE_CHANNEL, event.getUserId());
    }

    @Subscribe
    void onUserOffline(UserOfflineEvent event) {
        UserManager.INSTANCE.recordUserOffline(event.getUserId());
        RedisManager.I.publish(OFFLINE_CHANNEL, event.getUserId());
    }
}
