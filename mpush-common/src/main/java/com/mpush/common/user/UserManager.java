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

import java.util.List;

import com.mpush.tools.MPushUtil;
import com.mpush.cache.redis.manager.RedisManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpush.cache.redis.RedisKey;

//查询使用
public final class UserManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);
    public static final UserManager INSTANCE = new UserManager();

    private final String ONLINE_KEY = RedisKey.getUserOnlineKey(MPushUtil.getExtranetAddress());

    public UserManager() {
        clearUserOnlineData();
    }

    public void clearUserOnlineData() {
        RedisManager.I.del(ONLINE_KEY);
    }

    public void recordUserOnline(String userId) {
        RedisManager.I.zAdd(ONLINE_KEY, userId);
        LOGGER.info("user online {}", userId);
    }

    public void recordUserOffline(String userId) {
        RedisManager.I.zRem(ONLINE_KEY, userId);
        LOGGER.info("user offline {}", userId);
    }

    //在线用户
    public long getOnlineUserNum() {
        return RedisManager.I.zCard(ONLINE_KEY);
    }

    //在线用户列表
    public List<String> getOnlineUserList(int start, int size) {
        if (size < 10) {
            size = 10;
        }
        return RedisManager.I.zrange(ONLINE_KEY, start, size - 1, String.class);
    }
}
