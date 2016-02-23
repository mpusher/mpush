package com.shinemo.mpush.core.router;

import com.google.common.eventbus.Subscribe;
import com.shinemo.mpush.api.event.UserOfflineEvent;
import com.shinemo.mpush.api.event.UserOnlineEvent;
import com.shinemo.mpush.common.EventBus;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UserManager extends com.shinemo.mpush.common.manage.user.UserManager{
    public static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    public UserManager() {
    	EventBus.INSTANCE.register(this);
	}
    
    @Subscribe
    void handlerUserOnlineEvent(UserOnlineEvent event) {
    	userOnline(event.getUserId());
    }
    
    @Subscribe
    void handlerUserOfflineEvent(UserOfflineEvent event) {
    	if(event.getUserId()==null){//链接超时
    		String userId = RouterCenter.INSTANCE.getLocalRouterManager().getUserIdByConnId(event.getConnection().getId());
    		if(StringUtils.isNotBlank(userId)){
    			userOffline(userId);
    		}
    	}else{ //解绑用户
    		userOffline(event.getUserId());
    	}
    	
    }
    
}
