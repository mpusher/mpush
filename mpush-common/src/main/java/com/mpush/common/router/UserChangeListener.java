package com.mpush.common.router;

import com.mpush.cache.redis.listener.ListenerDispatcher;
import com.mpush.cache.redis.listener.MessageListener;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.MPushUtil;
import com.mpush.tools.event.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/1/4.
 *
 * @author ohun@live.cn
 */
public class UserChangeListener extends EventConsumer implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserChangeListener.class);

    public static final String ONLINE_CHANNEL = "/mpush/online/";

    public static final String OFFLINE_CHANNEL = "/mpush/offline/";

    //只需要一台机器注册online、offline 消息通道
    public UserChangeListener() {
        if ("127.0.0.1".equals(MPushUtil.getLocalIp())) {
            ListenerDispatcher.I.subscribe(getOnlineChannel(), this);
            ListenerDispatcher.I.subscribe(getOfflineChannel(), this);
        } else {
            LOGGER.error("UserChangeListener is not localhost,required:{}, but:{}", "127.0.0.1", MPushUtil.getLocalIp());
        }
    }

    public String getOnlineChannel() {
        return ONLINE_CHANNEL;
    }

    public String getOfflineChannel() {
        return OFFLINE_CHANNEL;
    }

    public void userOnline(String userId) {
        RedisManager.I.publish(getOnlineChannel(), userId);
    }

    public void userOffline(String userId) {
        RedisManager.I.publish(getOnlineChannel(), userId);
    }

    @Override
    public void onMessage(String channel, String message) {
    }
}
