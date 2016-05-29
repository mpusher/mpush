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

package com.mpush.cache.redis.manager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mpush.cache.redis.RedisException;
import com.mpush.cache.redis.RedisGroup;
import com.mpush.cache.redis.RedisServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKRedisNodeWatcher;
import com.mpush.zk.node.ZKRedisNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mpush.zk.ZKPath.REDIS_SERVER;

public class ZKRedisClusterManager implements RedisClusterManager {
    public static final ZKRedisClusterManager I = new ZKRedisClusterManager();

    private ZKRedisClusterManager() {
    }

    private final List<RedisGroup> groups = new ArrayList<>();

    /**
     * zk 启动的时候需要调用这个
     */
    @Override
    public void init() {
        Logs.Console.error("begin init redis cluster");
        if (!ZKClient.I.isRunning()) throw new RedisException("init redis cluster ex, ZK client not running.");
        List<com.mpush.tools.config.data.RedisGroup> groupList = CC.mp.redis.cluster_group;
        if (groupList.size() > 0) {
            if (CC.mp.redis.write_to_zk) {
                register(groupList);
            } else if (!ZKClient.I.isExisted(REDIS_SERVER.getRootPath())) {
                register(groupList);
            } else if (Strings.isNullOrEmpty(ZKClient.I.get(REDIS_SERVER.getRootPath()))) {
                register(groupList);
            }
        }

        ZKRedisNodeWatcher watcher = new ZKRedisNodeWatcher();
        watcher.beginWatch();
        Collection<ZKRedisNode> nodes = watcher.getCache().values();
        if (nodes == null || nodes.isEmpty()) {
            Logs.REDIS.info("init redis client error, redis server is none.");
            throw new RedisException("init redis client error, redis server is none.");
        }
        for (ZKRedisNode node : nodes) {
            groups.add(RedisGroup.from(node));
        }
        if (groups.isEmpty()) throw new RedisException("init redis sever fail groupList is null");
        Logs.Console.error("init redis cluster success...");
    }

    @Override
    public List<RedisGroup> getGroupList() {
        return Collections.unmodifiableList(groups);
    }

    public int groupSize() {
        return groups.size();
    }

    /**
     * 随机获取一个redis 实例
     *
     * @param key
     * @return
     */
    @Override
    public RedisServer randomGetRedisNode(String key) {
        int size = groupSize();
        if (size == 1) return groups.get(0).get(key);
        int i = (int) ((Math.random() % size) * size);
        RedisGroup group = groups.get(i);
        return group.get(key);
    }

    /**
     * 写操作的时候，获取所有redis 实例
     *
     * @param key
     * @return
     */
    @Override
    public List<RedisServer> hashSet(String key) {
        List<RedisServer> nodeList = Lists.newArrayList();
        for (RedisGroup group : groups) {
            RedisServer node = group.get(key);
            nodeList.add(node);
        }
        return nodeList;
    }

    private void register(List<com.mpush.tools.config.data.RedisGroup> groupList) {
        String data = Jsons.toJson(groupList);
        ZKClient.I.registerPersist(REDIS_SERVER.getRootPath(), data);
        Logs.Console.error("register redis server group success, group=" + data);
    }

    public void addGroup(RedisGroup group) {
        groups.add(group);
    }
}
