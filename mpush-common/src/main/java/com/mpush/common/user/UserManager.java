/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.common.user;

import com.mpush.cache.redis.RedisKey;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//查询使用
public final class UserManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);
    public static final UserManager I = new UserManager();

    private final String onlineUserListKey = RedisKey.getOnlineUserListKey(ConfigManager.I.getPublicIp());

    public void clearUserOnlineData()  {
        RedisManager.I.del(onlineUserListKey);
    }

    public void addToOnlineList(String userId) {
        RedisManager.I.zAdd(onlineUserListKey, userId);
        LOGGER.info("user online {}", userId);
    }

    public void remFormOnlineList(String userId) {
        RedisManager.I.zRem(onlineUserListKey, userId);
        LOGGER.info("user offline {}", userId);
    }

    //在线用户数量
    public long getOnlineUserNum() {
        Long value = RedisManager.I.zCard(onlineUserListKey);
        return value == null ? 0 : value;
    }

    //在线用户数量
    public long getOnlineUserNum(String publicIP) {
        String online_key = RedisKey.getOnlineUserListKey(publicIP);
        Long value = RedisManager.I.zCard(online_key);
        return value == null ? 0 : value;
    }

    //在线用户列表
    public List<String> getOnlineUserList(int start, int size) {
        if (size < 10) {
            size = 10;
        }
        return RedisManager.I.zrange(onlineUserListKey, start, size - 1, String.class);
    }
}
