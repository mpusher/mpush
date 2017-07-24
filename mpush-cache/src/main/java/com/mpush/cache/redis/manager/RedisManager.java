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

import com.google.common.collect.Lists;
import com.mpush.api.spi.common.*;
import com.mpush.cache.redis.connection.RedisConnectionFactory;
import com.mpush.tools.Jsons;
import com.mpush.tools.Utils;
import com.mpush.tools.config.CC;
import com.mpush.tools.log.Logs;
import com.mpush.monitor.service.ThreadPoolManager;
import redis.clients.jedis.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * redis 对外封装接口
 */
public final class RedisManager implements CacheManager {
    public static final RedisManager I = new RedisManager();

    private final RedisConnectionFactory factory = new RedisConnectionFactory();

    public void init() {
        Logs.CACHE.info("begin init redis...");
        factory.setPassword(CC.mp.redis.password);
        factory.setPoolConfig(CC.mp.redis.getPoolConfig(JedisPoolConfig.class));
        factory.setRedisServers(CC.mp.redis.nodes);
        factory.setCluster(CC.mp.redis.isCluster());
        if (CC.mp.redis.isSentinel()) {
            factory.setSentinelMaster(CC.mp.redis.sentinelMaster);
        }
        factory.init();
        test();
        Logs.CACHE.info("init redis success...");
    }

    private <R> R call(Function<JedisCommands, R> function, R d) {
        if (factory.isCluster()) {
            try {
                return function.apply(factory.getClusterConnection());
            } catch (Exception e) {
                Logs.CACHE.error("redis ex", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Jedis jedis = factory.getJedisConnection()) {
                return function.apply(jedis);
            } catch (Exception e) {
                Logs.CACHE.error("redis ex", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void call(Consumer<JedisCommands> consumer) {
        if (factory.isCluster()) {
            try {
                consumer.accept(factory.getClusterConnection());
            } catch (Exception e) {
                Logs.CACHE.error("redis ex", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Jedis jedis = factory.getJedisConnection()) {
                consumer.accept(jedis);
            } catch (Exception e) {
                Logs.CACHE.error("redis ex", e);
                throw new RuntimeException(e);
            }
        }
    }

    public long incr(String key) {
        return call(jedis -> jedis.incr(key), 0L);
    }

    public long incrBy(String key, long delt) {
        return call(jedis -> jedis.incrBy(key, delt), 0L);
    }

    /********************* k v redis start ********************************/
    /**
     * @param key
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        String value = call(jedis -> jedis.get(key), null);
        if (value == null) return null;
        if (clazz == String.class) return (T) value;
        return Jsons.fromJson(value, clazz);
    }

    public void set(String key, String value) {
        set(key, value, 0);
    }

    public void set(String key, Object value) {
        set(key, value, 0);
    }

    public void set(String key, Object value, int time) {
        set(key, Jsons.toJson(value), time);
    }

    /**
     * @param key
     * @param value
     * @param time  seconds
     */
    public void set(String key, String value, int time) {
        call(jedis -> {
            jedis.set(key, value);
            if (time > 0) {
                jedis.expire(key, time);
            }
        });
    }

    public void del(String key) {
        call(jedis -> jedis.del(key));
    }

    /********************* k v redis end ********************************/

    /*********************
     * hash redis start
     ********************************/
    public void hset(String key, String field, String value) {
        call(jedis -> jedis.hset(key, field, value));
    }

    public void hset(String key, String field, Object value) {
        hset(key, field, Jsons.toJson(value));
    }

    @SuppressWarnings("unchecked")
    public <T> T hget(String key, String field, Class<T> clazz) {
        String value = call(jedis -> jedis.hget(key, field), null);
        if (value == null) return null;
        if (clazz == String.class) return (T) value;
        return Jsons.fromJson(value, clazz);
    }

    public void hdel(String key, String field) {
        call(jedis -> jedis.hdel(key, field));
    }

    public Map<String, String> hgetAll(String key) {
        return call(jedis -> jedis.hgetAll(key), Collections.<String, String>emptyMap());
    }

    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<String, String> result = hgetAll(key);
        if (result.isEmpty()) return Collections.emptyMap();
        Map<String, T> newMap = new HashMap<>(result.size());
        result.forEach((k, v) -> newMap.put(k, Jsons.fromJson(v, clazz)));
        return newMap;
    }

    /**
     * 返回 key 指定的哈希集中所有字段的名字。
     *
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {
        return call(jedis -> jedis.hkeys(key), Collections.<String>emptySet());
    }

    /**
     * 返回 key 指定的哈希集中指定字段的值
     *
     * @param fields
     * @param clazz
     * @return
     */
    public <T> List<T> hmget(String key, Class<T> clazz, String... fields) {
        return call(jedis -> jedis.hmget(key, fields), Collections.<String>emptyList())
                .stream()
                .map(s -> Jsons.fromJson(s, clazz))
                .collect(Collectors.toList());

    }

    /**
     * 设置 key 指定的哈希集中指定字段的值。该命令将重写所有在哈希集中存在的字段。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key
     * 关联
     *
     * @param hash
     * @param time
     */
    public void hmset(String key, Map<String, String> hash, int time) {
        call(jedis -> {
            jedis.hmset(key, hash);
            if (time > 0) {
                jedis.expire(key, time);
            }
        });
    }

    public void hmset(String key, Map<String, String> hash) {
        hmset(key, hash, 0);
    }

    public long hincrBy(String key, String field, long value) {
        return call(jedis -> jedis.hincrBy(key, field, value), 0L);
    }

    /********************* hash redis end ********************************/

    /********************* list redis start ********************************/
    /**
     * 从队列的左边入队
     */
    public void lpush(String key, String... value) {
        call(jedis -> jedis.lpush(key, value));
    }

    public void lpush(String key, Object value) {
        lpush(key, Jsons.toJson(value));
    }

    /**
     * 从队列的右边入队
     */
    public void rpush(String key, String value) {
        call(jedis -> jedis.lpush(key, value));
    }

    public void rpush(String key, Object value) {
        rpush(key, Jsons.toJson(value));
    }

    /**
     * 移除并且返回 key 对应的 list 的第一个元素
     */
    @SuppressWarnings("unchecked")
    public <T> T lpop(String key, Class<T> clazz) {
        String value = call(jedis -> jedis.lpop(key), null);
        if (value == null) return null;
        if (clazz == String.class) return (T) value;
        return Jsons.fromJson(value, clazz);
    }

    /**
     * 从队列的右边出队一个元素
     */
    @SuppressWarnings("unchecked")
    public <T> T rpop(String key, Class<T> clazz) {
        String value = call(jedis -> jedis.rpop(key), null);
        if (value == null) return null;
        if (clazz == String.class) return (T) value;
        return Jsons.fromJson(value, clazz);
    }

    /**
     * 从列表中获取指定返回的元素 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        return call(jedis -> jedis.lrange(key, start, end), Collections.<String>emptyList())
                .stream()
                .map(s -> Jsons.fromJson(s, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 返回存储在 key 里的list的长度。 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。 当存储在 key
     * 里的值不是一个list的话，会返回error。
     */
    public long llen(String key) {
        return call(jedis -> jedis.llen(key), 0L);
    }

    /**
     * 移除表中所有与 value 相等的值
     *
     * @param key
     * @param value
     */
    public void lRem(String key, String value) {
        call(jedis -> jedis.lrem(key, 0, value));
    }

    /********************* list redis end ********************************/

    /*********************
     * mq redis start
     ********************************/


    public void publish(String channel, Object message) {
        String msg = message instanceof String ? (String) message : Jsons.toJson(message);
        call(jedis -> {
            if (jedis instanceof MultiKeyCommands) {
                ((MultiKeyCommands) jedis).publish(channel, msg);
            } else if (jedis instanceof MultiKeyJedisClusterCommands) {
                ((MultiKeyJedisClusterCommands) jedis).publish(channel, msg);
            }
        });
    }

    public void subscribe(final JedisPubSub pubsub, final String channel) {
        Utils.newThread(channel,
                () -> call(jedis -> {
                    if (jedis instanceof MultiKeyCommands) {
                        ((MultiKeyCommands) jedis).subscribe(pubsub, channel);
                    } else if (jedis instanceof MultiKeyJedisClusterCommands) {
                        ((MultiKeyJedisClusterCommands) jedis).subscribe(pubsub, channel);
                    }
                })
        ).start();
    }

    /*********************
     * set redis start
     ********************************/
    /**
     * @param key
     * @param value
     */
    public void sAdd(String key, String value) {
        call(jedis -> jedis.sadd(key, value));
    }

    /**
     * @param key
     * @return
     */
    public long sCard(String key) {
        return call(jedis -> jedis.scard(key), 0L);
    }

    public void sRem(String key, String value) {
        call(jedis -> jedis.srem(key, value));
    }

    /**
     * 默认使用每页10个
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> sScan(String key, Class<T> clazz, int start) {
        List<String> list = call(jedis -> jedis.sscan(key, Integer.toString(start), new ScanParams().count(10)).getResult(), null);
        return toList(list, clazz);
    }

    /*********************
     * sorted set
     ********************************/
    /**
     * @param key
     * @param value
     */
    public void zAdd(String key, String value) {
        call(jedis -> jedis.zadd(key, 0, value));
    }

    /**
     * @param key
     * @return
     */
    public Long zCard(String key) {
        return call(jedis -> jedis.zcard(key), 0L);
    }

    public void zRem(String key, String value) {
        call(jedis -> jedis.zrem(key, value));
    }

    /**
     * 从列表中获取指定返回的元素 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        Set<String> value = call(jedis -> jedis.zrange(key, start, end), null);
        return toList(value, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> toList(Collection<String> value, Class<T> clazz) {
        if (value != null) {
            if (clazz == String.class) {
                return (List<T>) new ArrayList<>(value);
            }
            List<T> newValue = Lists.newArrayList();
            for (String temp : value) {
                newValue.add(Jsons.fromJson(temp, clazz));
            }
            return newValue;
        }
        return null;
    }

    public void destroy() {
        if (factory != null) factory.destroy();
    }

    public void test() {
        if (factory.isCluster()) {
            JedisCluster cluster = factory.getClusterConnection();
            if (cluster == null) throw new RuntimeException("init redis cluster error.");
        } else {
            Jedis jedis = factory.getJedisConnection();
            if (jedis == null) throw new RuntimeException("init redis error, can not get connection.");
            jedis.close();
        }
    }
}
