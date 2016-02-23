package com.shinemo.mpush.common.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.common.AbstractEventContainer;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.redis.listener.ListenerDispatcher;
import com.shinemo.mpush.tools.redis.listener.MessageListener;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

/**
 * Created by ohun on 2016/1/4.
 */
public class UserChangeListener extends AbstractEventContainer implements MessageListener {
	
	private static final Logger log = LoggerFactory.getLogger(UserChangeListener.class);
	
    public static final String ONLINE_CHANNEL = "/mpush/online/";
    
    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    //只需要一台机器注册online、offline 消息通道
    public UserChangeListener() {
    	if(ConfigCenter.holder.onlineAndOfflineListenerIp().equals(MPushUtil.getLocalIp())){
    		ListenerDispatcher.INSTANCE.subscribe(getOnlineChannel(), this);
    		ListenerDispatcher.INSTANCE.subscribe(getOfflineChannel(), this);
    	}else{
    		log.error("UserChangeListener is not localhost,required:{},but:{}",ConfigCenter.holder.onlineAndOfflineListenerIp(),MPushUtil.getLocalIp());
    	}
    }

    public String getOnlineChannel() {
        return ONLINE_CHANNEL;
    }
    
    public String getOfflineChannel() {
        return OFFLINE_CHANNEL;
    }
    
    public void userOnline(String userId) {
        RedisManage.publish(getOnlineChannel(), userId);
    }
    
    public void userOffline(String userId){
    	RedisManage.publish(getOnlineChannel(), userId);
    }

    @Override
    public void onMessage(String channel, String message) {
    }
}
