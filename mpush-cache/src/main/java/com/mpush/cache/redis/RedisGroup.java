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

package com.mpush.cache.redis;

import com.google.common.collect.Lists;

import java.util.List;


/**
 * redis 组
 */
public class RedisGroup {
    private List<RedisServer> redisServerList = Lists.newArrayList();

    public List<RedisServer> getRedisServerList() {
        return redisServerList;
    }

    public void setRedisServerList(List<RedisServer> redisServerList) {
        this.redisServerList = redisServerList;
    }

    public void addRedisNode(RedisServer node) {
        redisServerList.add(node);
    }

    public void remove(int i) {
        if (redisServerList != null) {
            redisServerList.remove(i);
        }
    }

    public void clear() {
        if (redisServerList != null) {
            redisServerList.clear();
        }
    }

    public RedisServer get(String key) {
        if (redisServerList.size() == 1) return redisServerList.get(0);
        int i = key.hashCode() % redisServerList.size();
        return redisServerList.get(i);
    }

    public static RedisGroup from(com.mpush.tools.config.data.RedisGroup node) {
        RedisGroup group = new RedisGroup();
        for (com.mpush.tools.config.data.RedisServer rs : node.redisNodeList) {
            group.addRedisNode(new RedisServer(rs.getHost(), rs.getPort(), rs.getPassword()));
        }
        return group;
    }
}
