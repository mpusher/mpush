package com.mpush.common.router;

import com.mpush.tools.redis.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpush.common.AbstractEventContainer;
import com.mpush.tools.MPushUtil;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.redis.listener.ListenerDispatcher;
import com.mpush.tools.redis.manage.RedisManage;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public class UserChangeListener extends AbstractEventContainer implements MessageListener {
	
	private static final Logger log = LoggerFactory.getLogger(UserChangeListener.class);
	
    public static final String ONLINE_CHANNEL = "/mpush/online/";
    
    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    //只需要一台机器注册online、offline 消息通道
    public UserChangeListener() {
    	if(ConfigCenter.I.onlineAndOfflineListenerIp().equals(MPushUtil.getLocalIp())){
    		ListenerDispatcher.INSTANCE.subscribe(getOnlineChannel(), this);
    		ListenerDispatcher.INSTANCE.subscribe(getOfflineChannel(), this);
    	}else{
    		log.error("UserChangeListener is not localhost,required:{},but:{}",ConfigCenter.I.onlineAndOfflineListenerIp(),MPushUtil.getLocalIp());
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
