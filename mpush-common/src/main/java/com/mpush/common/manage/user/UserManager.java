package com.mpush.common.manage.user;

import java.util.List;

import com.mpush.tools.MPushUtil;
import com.mpush.tools.redis.manage.RedisManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpush.api.RedisKey;

//查询使用
public final class UserManager {
    private static final Logger log = LoggerFactory.getLogger(UserManager.class);
    public static final UserManager INSTANCE = new UserManager();

    private final String ONLINE_KEY = RedisKey.getUserOnlineKey(MPushUtil.getExtranetAddress());

    public UserManager() {
        clearUserOnlineData();
    }

    public void clearUserOnlineData() {
        RedisManage.del(ONLINE_KEY);
    }

    public void recordUserOnline(String userId) {
        RedisManage.zAdd(ONLINE_KEY, userId);
        log.info("user online {}", userId);
    }

    public void recordUserOffline(String userId) {
        RedisManage.zRem(ONLINE_KEY, userId);
        log.info("user offline {}", userId);
    }

    //在线用户
    public long getOnlineUserNum() {
        return RedisManage.zCard(ONLINE_KEY);
    }

    //在线用户列表
    public List<String> getOnlineUserList(int start, int size) {
        if (size < 10) {
            size = 10;
        }
        return RedisManage.zrange(ONLINE_KEY, start, size - 1, String.class);
    }
}
