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

import com.google.common.collect.Sets;
import com.mpush.cache.redis.RedisClient;
import com.mpush.cache.redis.RedisGroup;
import com.mpush.cache.redis.RedisServer;
import com.mpush.tools.Jsons;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis 对外封装接口
 */
public final class RedisManager {
    public static final RedisManager I = new RedisManager();

    private final RedisClusterManager clusterManager = ZKRedisClusterManager.I;

    public void init() {
        ZKRedisClusterManager.I.init();
        test(clusterManager.getGroupList());
    }

    public long incr(String key, Integer time) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        return RedisClient.incr(nodeList, key, time);
    }

    public long incrBy(String key, long delt) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        return RedisClient.incrBy(nodeList, key, delt);
    }

    /*********************
     * k v redis start
     ********************************/

    public <T> T get(String key, Class<T> clazz) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.get(node, key, clazz);
    }

    public <T> void set(String key, T value) {
        set(key, value, null);
    }

    public <T> void set(String key, T value, Integer time) {
        String jsonValue = Jsons.toJson(value);
        set(key, jsonValue, time);
    }

    /**
     * @param key
     * @param value
     * @param time  seconds
     */
    public void set(String key, String value, Integer time) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.set(nodeList, key, value, time);
    }

    public void del(String key) {

        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.del(nodeList, key);

    }

    /*********************k v redis end********************************/


    /*********************
     * hash redis start
     ********************************/
    public void hset(String key, String field, String value) {

        List<RedisServer> nodeList = clusterManager.hashSet(field);
        RedisClient.hset(nodeList, key, field, value);

    }

    public <T> void hset(String key, String field, T value) {
        hset(key, field, Jsons.toJson(value));
    }

    public <T> T hget(String key, String field, Class<T> clazz) {

        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.hget(node, key, field, clazz);

    }

    public void hdel(String key, String field) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.hdel(nodeList, key, field);
    }

    public Map<String, String> hgetAll(String key) {

        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.hgetAll(node, key);

    }

    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.hgetAll(node, key, clazz);
    }

    /**
     * 返回 key 指定的哈希集中所有字段的名字。
     *
     * @return
     */
    public Set<String> hkeys(String key) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.hkeys(node, key);
    }

    /**
     * 返回 key 指定的哈希集中指定字段的值
     *
     * @param fields
     * @param clazz
     * @return
     */
    public <T> List<T> hmget(String key, Class<T> clazz, String... fields) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.hmget(node, key, clazz, fields);
    }

    /**
     * 设置 key 指定的哈希集中指定字段的值。该命令将重写所有在哈希集中存在的字段。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联
     *
     * @param hash
     * @param time
     */
    public void hmset(String key, Map<String, String> hash, Integer time) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.hmset(nodeList, key, hash, time);
    }

    public void hmset(String key, Map<String, String> hash) {
        hmset(key, hash, null);
    }


    /*********************hash redis end********************************/


    /*********************list redis start********************************/
    /**
     * 从队列的左边入队
     */
    public void lpush(String key, String value) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.lpush(nodeList, key, value);
    }

    public <T> void lpush(String key, T value) {
        lpush(key, Jsons.toJson(value));
    }

    /**
     * 从队列的右边入队
     */
    public void rpush(String key, String value) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.rpush(nodeList, key, value);
    }

    public <T> void rpush(String key, T value) {
        rpush(key, Jsons.toJson(value));
    }

    /**
     * 移除并且返回 key 对应的 list 的第一个元素
     */
    public <T> T lpop(String key, Class<T> clazz) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        return RedisClient.lpop(nodeList, key, clazz);
    }

    /**
     * 从队列的右边出队一个元素
     */
    public <T> T rpop(String key, Class<T> clazz) {
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        return RedisClient.rpop(nodeList, key, clazz);
    }


    /**
     * 从列表中获取指定返回的元素
     * start 和 end 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.lrange(node, key, start, end, clazz);
    }

    /**
     * 返回存储在 key 里的list的长度。 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。 当存储在 key 里的值不是一个list的话，会返回error。
     */
    public long llen(String key) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.llen(node, key);
    }

    public <T> void lrem(String key, T value) {
        String jsonValue = Jsons.toJson(value);
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.lRem(nodeList, key, jsonValue);
    }

    public <T> void publish(String channel, T message) {

        RedisServer node = clusterManager.randomGetRedisNode(channel);
        RedisClient.publish(node, channel, message);

    }

    public void subscribe(JedisPubSub pubsub, String... channels) {

        Set<RedisServer> set = Sets.newHashSet();
        for (String channel : channels) {
            List<RedisServer> nodeList = clusterManager.hashSet(channel);
            set.addAll(nodeList);
        }

        RedisClient.subscribe(set, pubsub, channels);
    }

    public <T> void sAdd(String key, T value) {
        String jsonValue = Jsons.toJson(value);
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.sAdd(nodeList, key, jsonValue);
    }

    public Long sCard(String key) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.sCard(node, key);
    }

    public <T> void sRem(String key, T value) {
        String jsonValue = Jsons.toJson(value);
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.sRem(nodeList, key, jsonValue);
    }

    public <T> List<T> sScan(String key, int start, Class<T> clazz) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.sScan(node, key, clazz, start);
    }

    public <T> void zAdd(String key, T value) {
        String jsonValue = Jsons.toJson(value);
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.zAdd(nodeList, key, jsonValue);
    }

    public Long zCard(String key) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.zCard(node, key);
    }

    public <T> void zRem(String key, T value) {
        String jsonValue = Jsons.toJson(value);
        List<RedisServer> nodeList = clusterManager.hashSet(key);
        RedisClient.zRem(nodeList, key, jsonValue);
    }

    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        RedisServer node = clusterManager.randomGetRedisNode(key);
        return RedisClient.zrange(node, key, start, end, clazz);
    }

    public void test(List<RedisGroup> groupList) {
        if (groupList == null || groupList.isEmpty()) {
            throw new RuntimeException("init redis sever error.");
        }
        for (RedisGroup group : groupList) {
            List<RedisServer> list = group.getRedisServerList();
            if (list == null || list.isEmpty()) {
                throw new RuntimeException("init redis sever error.");
            }
            for (RedisServer node : list) {
                Jedis jedis = RedisClient.getClient(node);
                if (jedis == null) throw new RuntimeException("init redis sever error.");
                jedis.close();
            }
        }
    }

    public void close() {
        RedisClient.destroy();
    }
}
