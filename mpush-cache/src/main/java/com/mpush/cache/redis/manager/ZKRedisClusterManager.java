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

import com.mpush.cache.redis.RedisException;
import com.mpush.cache.redis.RedisServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.data.RedisNode;
import com.mpush.tools.log.Logs;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKRedisNodeWatcher;
import com.mpush.zk.node.ZKRedisNode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.mpush.zk.ZKPath.REDIS_SERVER;

public class ZKRedisClusterManager implements RedisClusterManager {
    private final ZKRedisNodeWatcher watcher = new ZKRedisNodeWatcher();

    public ZKRedisClusterManager() {
    }

    /**
     * zk 启动的时候需要调用这个
     */
    @Override
    public void init() {
        if (!ZKClient.I.isRunning()) throw new RedisException("init redis cluster ex, ZK client not running.");

        if (CollectionUtils.isNotEmpty(CC.mp.redis.nodes)) {
            register(CC.mp.redis.nodes);
        }

        watcher.watch();
        Collection<ZKRedisNode> nodes = watcher.getCache().values();
        if (CollectionUtils.isEmpty(nodes)) {
            Logs.REDIS.error("init redis client error, redis server is none.");
            throw new RedisException("init redis client error, redis server is none.");
        }

        if (nodes.isEmpty()) throw new RedisException("init redis sever fail groupList is null");
    }

    @Override
    public List<RedisServer> getServers() {
        return watcher.getCache().values().stream().map(RedisServer::from).collect(Collectors.toList());
    }

    private void register(List<RedisNode> nodes) {
        String data = Jsons.toJson(nodes);
        if (CC.mp.redis.write_to_zk //强制刷新ZK
                || !ZKClient.I.isExisted(REDIS_SERVER.getRootPath())//redis节点不存在
                || !ZKClient.I.get(REDIS_SERVER.getRootPath()).equals(data)) {//数据有变更
            ZKClient.I.registerPersist(REDIS_SERVER.getRootPath(), data);
            Logs.Console.info("register redis server group success, group={}", data);
        }
    }
}
