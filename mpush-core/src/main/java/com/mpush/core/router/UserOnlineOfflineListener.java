package com.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.common.EventBus;
import com.mpush.common.manage.user.UserManager;
import com.mpush.tools.redis.manage.RedisManage;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class UserOnlineOfflineListener {

    public static final String ONLINE_CHANNEL = "/mpush/online/";

    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    public UserOnlineOfflineListener() {
        EventBus.INSTANCE.register(this);
    }

    @Subscribe
    void onUserOnline(UserOnlineEvent event) {
        UserManager.INSTANCE.recordUserOnline(event.getUserId());
        RedisManage.publish(ONLINE_CHANNEL, event.getUserId());
    }

    @Subscribe
    void onUserOffline(UserOfflineEvent event) {
        UserManager.INSTANCE.recordUserOffline(event.getUserId());
        RedisManage.publish(OFFLINE_CHANNEL, event.getUserId());
    }
}
