package com.shinemo.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.event.UserOfflineEvent;
import com.shinemo.mpush.api.event.UserOnlineEvent;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UserManager extends com.shinemo.mpush.common.manage.user.UserManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    public static final String ONLINE_CHANNEL = "/mpush/online/";

    public static final String OFFLINE_CHANNEL = "/mpush/offline/";
    
    public UserManager() {
        EventBus.INSTANCE.register(this);
        init();
    }

    @Subscribe
    void handlerUserOnlineEvent(UserOnlineEvent event) {
        userOnline(event.getUserId());
        RedisManage.publish(ONLINE_CHANNEL, event.getUserId());
    }

    @Subscribe
    void handlerUserOfflineEvent(UserOfflineEvent event) {
//        if (event.getUserId() == null) {//链接超时
//            String userId = RouterCenter.INSTANCE.getLocalRouterManager().getUserIdByConnId(event.getConnection().getId());
//            if (StringUtils.isNotBlank(userId)) {
//                userOffline(userId);
//                RedisManage.publish(OFFLINE_CHANNEL, event.getUserId());
//            }
//        } else { //解绑用户
//            userOffline(event.getUserId());
//            RedisManage.publish(OFFLINE_CHANNEL, event.getUserId());
//        }
    	if(event.getUserId()==null){
    		return;
    	}
    	userOffline(event.getUserId());
        RedisManage.publish(OFFLINE_CHANNEL, event.getUserId());
    }
}
