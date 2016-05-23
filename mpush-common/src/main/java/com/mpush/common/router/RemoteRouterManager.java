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

package com.mpush.common.router;

import com.mpush.cache.redis.RedisKey;
import com.mpush.api.router.RouterManager;
import com.mpush.cache.redis.manager.RedisManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public class RemoteRouterManager implements RouterManager<RemoteRouter> {
    public static final Logger LOGGER = LoggerFactory.getLogger(RemoteRouterManager.class);

    @Override
    public RemoteRouter register(String userId, RemoteRouter router) {
    	LOGGER.info("register remote router success userId={}, router={}", userId, router);
    	String key = RedisKey.getUserKey(userId);
        RemoteRouter old = RedisManager.I.get(key, RemoteRouter.class);
        if (old != null) {
            RedisManager.I.del(key);
        }
        RedisManager.I.set(key, router);
        return old;
    }

    @Override
    public boolean unRegister(String userId) {
    	String key = RedisKey.getUserKey(userId);
        RedisManager.I.del(key);
        LOGGER.info("unRegister remote router success userId={}", userId);
        return true;
    }

    @Override
    public RemoteRouter lookup(String userId) {
    	String key = RedisKey.getUserKey(userId);
        return RedisManager.I.get(key, RemoteRouter.class);
    }
}
