package com.shinemo.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.event.UserOfflineEvent;
import com.shinemo.mpush.api.event.UserOnlineEvent;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.manage.user.UserManager;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UserOnlineOfflineListener {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserOnlineOfflineListener.class);

    public static final String ONLINE_CHANNEL = "/mpush/online/";

    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    public UserOnlineOfflineListener() {
        EventBus.INSTANCE.register(this);
    }

    @Subscribe
    void onUserOnline(UserOnlineEvent event) {
        UserManager.INSTANCE.userOnline(event.getUserId());
        RedisManage.publish(ONLINE_CHANNEL, event.getUserId());
    }

    @Subscribe
    void onUserOffline(UserOfflineEvent event) {
        UserManager.INSTANCE.userOffline(event.getUserId());
        RedisManage.publish(OFFLINE_CHANNEL, event.getUserId());
    }
}
