package com.mpush.cache.redis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mpush.tools.log.Logs;
import com.mpush.tools.Jsons;
import redis.clients.jedis.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisClient {
    private static final int REDIS_TIMEOUT = 2000;
    private static final int REDIS_MAX_TOTAL = 8;
    private static final int REDIS_MAX_IDLE = 4;
    private static final int REDIS_MIN_IDLE = 1;
    private static final int REDIS_MAX_WAIT_MILLIS = 5000;
    private static final int REDIS_MIN_EVICTABLE_IDLE_TIME_MILLIS = 300000;
    private static final int REDIS_NUM_TESTS_PER_EVICTION_RUN = 3;
    private static final int REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000;
    private static final boolean REDIS_TEST_ON_BORROW = false;
    private static final boolean REDIS_TEST_ON_RETURN = false;
    private static final boolean REDIS_TEST_WHILE_IDLE = false;

    public static final JedisPoolConfig CONFIG = new JedisPoolConfig();
    private static final Map<RedisServer, JedisPool> POOL_MAP = Maps.newConcurrentMap();

    static {
        //连接池中最大连接数。高版本：maxTotal，低版本：maxActive
        CONFIG.setMaxTotal(REDIS_MAX_TOTAL);
        //连接池中最大空闲的连接数
        CONFIG.setMaxIdle(REDIS_MAX_IDLE);
        //连接池中最少空闲的连接数
        CONFIG.setMinIdle(REDIS_MIN_IDLE);
        //当连接池资源耗尽时，调用者最大阻塞的时间，超时将跑出异常。单位，毫秒数;默认为-1.表示永不超时。高版本：maxWaitMillis，低版本：maxWait
        CONFIG.setMaxWaitMillis(REDIS_MAX_WAIT_MILLIS);
        //连接空闲的最小时间，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除
        CONFIG.setMinEvictableIdleTimeMillis(REDIS_MIN_EVICTABLE_IDLE_TIME_MILLIS);
        //对于“空闲链接”检测线程而言，每次检测的链接资源的个数。默认为3
        CONFIG.setNumTestsPerEvictionRun(REDIS_NUM_TESTS_PER_EVICTION_RUN);
        //“空闲链接”检测线程，检测的周期，毫秒数。如果为负值，表示不运行“检测线程”。默认为-1
        CONFIG.setTimeBetweenEvictionRunsMillis(REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        //testOnBorrow:向调用者输出“链接”资源时，是否检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取。默认为false。建议保持默认值.
        CONFIG.setTestOnBorrow(REDIS_TEST_ON_BORROW);
        //testOnReturn:向连接池“归还”链接时，是否检测“链接”对象的有效性。默认为false。建议保持默认值
        CONFIG.setTestOnReturn(REDIS_TEST_ON_RETURN);
        //testWhileIdle:向调用者输出“链接”对象时，是否检测它的空闲超时；默认为false。如果“链接”空闲超时，将会被移除。建议保持默认值.
        CONFIG.setTestWhileIdle(REDIS_TEST_WHILE_IDLE);
    }

    public static void main(String[] args) {
        System.out.println(Jsons.toJson(CONFIG));
    }

    public static Jedis getClient(RedisServer node) {
        JedisPool pool = POOL_MAP.get(node);
        if (pool == null) {
            pool = new JedisPool(CONFIG, node.getHost(), node.getPort(), REDIS_TIMEOUT, node.getPassword());
            POOL_MAP.put(node, pool);
        }
        return pool.getResource();
    }

    public static void close(Jedis jedis) {
        jedis.close();
    }

    public static long incr(List<RedisServer> nodeList, String key, Integer time) {
        long incrRet = -1;
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                long ret = jedis.incr(key);
                if (ret == 1 && time != null) {
                    jedis.expire(key, time);
                }
                incrRet = ret;
            } catch (Exception e) {
                Logs.REDIS.error("redis incr exception:{},{},{},{}", key, time, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
        return incrRet;

    }

    public static long incrBy(List<RedisServer> nodeList, String key, long delt) {
        long incrRet = -1;
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                long ret = jedis.incrBy(key, delt);
                incrRet = ret;
            } catch (Exception e) {
                Logs.REDIS.error("redis incr exception:{},{},{},{}", key, delt, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
        return incrRet;

    }

    /********************* k v redis start ********************************/
    /**
     * @param node  redis实例
     * @param key
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(RedisServer node, String key, Class<T> clazz) {

        String value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.get(key);
        } catch (Exception e) {
            Logs.REDIS.error("redis get exception:{},{}", key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (clazz == String.class) return (T) value;
        return Jsons.fromJson(value, clazz);
    }

    public static void set(List<RedisServer> nodeList, String key, String value) {

        set(nodeList, key, value, null);

    }

    public static <T> void set(List<RedisServer> nodeList, String key, T value) {
        set(nodeList, key, value, null);
    }

    public static <T> void set(List<RedisServer> nodeList, String key, T value, Integer time) {
        String jsonValue = Jsons.toJson(value);
        set(nodeList, key, jsonValue, time);
    }

    /**
     * @param nodeList
     * @param key
     * @param value
     * @param time     seconds
     */
    public static void set(List<RedisServer> nodeList, String key, String value, Integer time) {
        if (time == null) {
            time = -1;
        }
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.set(key, value);
                if (time > 0) {
                    jedis.expire(key, time);
                }
            } catch (Exception e) {
                Logs.REDIS.error("redis set exception:{},{},{},{}", key, value, time, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    public static void del(List<RedisServer> nodeList, String key) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.del(key);
            } catch (Exception e) {
                Logs.REDIS.error("redis del exception:{},{}", key, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    /********************* k v redis end ********************************/

    /*********************
     * hash redis start
     ********************************/
    public static void hset(List<RedisServer> nodeList, String namespace, String key, String value) {
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.hset(namespace, key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis hset exception:{},{},{},{}", namespace, key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    public static <T> void hset(List<RedisServer> nodeList, String namespace, String key, T value) {
        hset(nodeList, namespace, key, Jsons.toJson(value));
    }

    public static <T> T hget(RedisServer node, String namespace, String key, Class<T> clazz) {

        String value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.hget(namespace, key);
        } catch (Exception e) {
            Logs.REDIS.error("redis hget exception:{},{}", namespace, key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        return Jsons.fromJson(value, clazz);

    }

    public static void hdel(List<RedisServer> nodeList, String namespace, String key) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.hdel(namespace, key);
            } catch (Exception e) {
                Logs.REDIS.error("redis hdel exception:{},{},{}", namespace, key, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    public static Map<String, String> hgetAll(RedisServer node, String namespace) {
        Map<String, String> result = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            result = jedis.hgetAll(namespace);
        } catch (Exception e) {
            Logs.REDIS.error("redis hgetAll exception:{},{}", namespace, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        return result;
    }

    public static <T> Map<String, T> hgetAll(RedisServer node, String namespace, Class<T> clazz) {
        Map<String, String> result = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            result = jedis.hgetAll(namespace);
        } catch (Exception e) {
            Logs.REDIS.error("redis hgetAll exception:{},{}", namespace, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (result != null) {
            Map<String, T> newMap = Maps.newHashMap();
            Iterator<Map.Entry<String, String>> iterator = result.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String val = entry.getValue();
                newMap.put(key, Jsons.fromJson(val, clazz));
            }
            return newMap;
        } else {
            return null;
        }

    }

    /**
     * 返回 key 指定的哈希集中所有字段的名字。
     *
     * @param node
     * @param key
     * @return
     */
    public static Set<String> hkeys(RedisServer node, String key) {
        Set<String> result = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            result = jedis.hkeys(key);
        } catch (Exception e) {
            Logs.REDIS.error("redis hkeys exception:{},{}", key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);

        }
        return result;
    }

    /**
     * 返回 key 指定的哈希集中指定字段的值
     *
     * @param node
     * @param key
     * @param clazz
     * @return
     */
    public static <T> List<T> hmget(RedisServer node, String namespace, Class<T> clazz, String... key) {

        List<String> value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.hmget(namespace, key);
        } catch (Exception e) {
            Logs.REDIS.error("redis hmget exception:{},{},{}", namespace, key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (value != null) {
            List<T> newValue = Lists.newArrayList();
            for (String temp : value) {
                newValue.add(Jsons.fromJson(temp, clazz));
            }
            return newValue;
        }
        return null;

    }

    /**
     * 设置 key 指定的哈希集中指定字段的值。该命令将重写所有在哈希集中存在的字段。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key
     * 关联
     *
     * @param nodeList
     * @param hash
     * @param time
     */
    public static void hmset(List<RedisServer> nodeList, String namespace, Map<String, String> hash, Integer time) {

        if (time == null) {
            time = -1;
        }
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.hmset(namespace, hash);
                if (time > 0) {
                    jedis.expire(namespace, time);
                }

            } catch (Exception e) {
                Logs.REDIS.error("redis hmset exception:{},{},{}", namespace, time, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    public static void hmset(List<RedisServer> nodeList, String namespace, Map<String, String> hash) {
        hmset(nodeList, namespace, hash, null);
    }

    /********************* hash redis end ********************************/

    /********************* list redis start ********************************/
    /**
     * 从队列的左边入队
     */
    public static void lpush(List<RedisServer> nodeList, String key, String value) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.lpush(key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis lpush exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    public static <T> void lpush(List<RedisServer> nodeList, String key, T value) {

        lpush(nodeList, key, Jsons.toJson(value));

    }

    /**
     * 从队列的右边入队
     */
    public static void rpush(List<RedisServer> nodeList, String key, String value) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.rpush(key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis rpush exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    public static <T> void rpush(List<RedisServer> nodeList, String key, T value) {
        rpush(nodeList, key, Jsons.toJson(value));
    }

    /**
     * 移除并且返回 key 对应的 list 的第一个元素
     */
    public static <T> T lpop(List<RedisServer> nodeList, String key, Class<T> clazz) {
        String retValue = null;
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            String vaule = null;
            try {
                jedis = getClient(node);
                vaule = jedis.lpop(key);
                retValue = vaule;
            } catch (Exception e) {
                Logs.REDIS.error("redis lpop exception:{},{}", key, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

        return Jsons.fromJson(retValue, clazz);
    }

    /**
     * 从队列的右边出队一个元素
     */
    public static <T> T rpop(List<RedisServer> nodeList, String key, Class<T> clazz) {
        String retValue = null;
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            String vaule = null;
            try {
                jedis = getClient(node);
                vaule = jedis.rpop(key);
                retValue = vaule;
            } catch (Exception e) {
                Logs.REDIS.error("redis rpop exception:{},{}", key, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

        return Jsons.fromJson(retValue, clazz);
    }

    /**
     * 从列表中获取指定返回的元素 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public static <T> List<T> lrange(RedisServer node, String key, int start, int end, Class<T> clazz) {
        List<String> value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.lrange(key, start, end);
        } catch (Exception e) {
            Logs.REDIS.error("redis lrange exception:{},{},{},{}", key, start, end, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (value != null) {
            List<T> newValue = Lists.newArrayList();
            for (String temp : value) {
                newValue.add(Jsons.fromJson(temp, clazz));
            }
            return newValue;
        }
        return null;
    }

    /**
     * 返回存储在 key 里的list的长度。 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。 当存储在 key
     * 里的值不是一个list的话，会返回error。
     */
    public static long llen(RedisServer node, String key) {

        long len = 0;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            len = jedis.llen(key);
        } catch (Exception e) {
            Logs.REDIS.error("redis llen exception:{},{}", key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        return len;
    }

    /**
     * 移除表中所有与 value 相等的值
     *
     * @param nodeList
     * @param key
     * @param value
     */
    public static void lRem(List<RedisServer> nodeList, String key, String value) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.lrem(key, 0, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis lrem exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    /********************* list redis end ********************************/

    /*********************
     * mq redis start
     ********************************/


    public static <T> void publish(RedisServer node, String channel, T message) {
        Jedis jedis = null;
        String value = null;
        if (message instanceof String) {
            value = (String) message;
        } else {
            value = Jsons.toJson(message);
        }
        try {
            jedis = getClient(node);
            jedis.publish(channel, value);
        } catch (Exception e) {
            Logs.REDIS.error("redis publish exception:{},{},{}", value, Jsons.toJson(node), Jsons.toJson(channel), e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
    }

    public static void subscribe(Set<RedisServer> nodeList, final JedisPubSub pubsub, final String... channels) {
        for (final RedisServer node : nodeList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    subscribe(node, pubsub, channels);
                }
            }, node.getHost() + "_" + Jsons.toJson(channels)
            ).start();
        }
    }

    public static void subscribe(RedisServer node, JedisPubSub pubsub, String... channel) {
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            jedis.subscribe(pubsub, channel);
        } catch (Exception e) {
            Logs.REDIS.error("redis subscribe exception:{},{}", Jsons.toJson(node), Jsons.toJson(channel), e);
        } finally {
            // 返还到连接池
            close(jedis);
        }

    }

    /*********************
     * set redis start
     ********************************/
    /**
     * @param nodeList
     * @param key
     * @param value
     */
    public static void sAdd(List<RedisServer> nodeList, String key, String value) {
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.sadd(key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis sadd exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    /**
     * @param node 返回个数
     * @param key
     * @return
     */
    public static Long sCard(RedisServer node, String key) {

        Long value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.scard(key);
        } catch (Exception e) {
            Logs.REDIS.error("redis scard exception:{},{}", key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        return value;
    }

    public static void sRem(List<RedisServer> nodeList, String key, String value) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.srem(key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis srem exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    /**
     * 默认使用每页10个
     *
     * @param node
     * @param key
     * @param clazz
     * @return
     */
    public static <T> List<T> sScan(RedisServer node, String key, Class<T> clazz, int start) {

        List<String> value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            ScanResult<String> sscanResult = jedis.sscan(key, start + "", new ScanParams().count(10));
            if (sscanResult != null && sscanResult.getResult() != null) {
                value = sscanResult.getResult();
            }
        } catch (Exception e) {
            Logs.REDIS.error("redis sscan exception:{},{},{}", key, start, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (value != null) {
            List<T> newValue = Lists.newArrayList();
            for (String temp : value) {
                newValue.add(Jsons.fromJson(temp, clazz));
            }
            return newValue;
        }
        return null;

    }

    /*********************
     * sorted set
     ********************************/
    /**
     * @param nodeList
     * @param key
     * @param value
     */
    public static void zAdd(List<RedisServer> nodeList, String key, String value) {
        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.zadd(key, 0, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis zadd exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }
    }

    /**
     * @param node 返回个数
     * @param key
     * @return
     */
    public static Long zCard(RedisServer node, String key) {

        Long value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.zcard(key);
        } catch (Exception e) {
            Logs.REDIS.error("redis zcard exception:{},{}", key, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        return value;
    }

    public static void zRem(List<RedisServer> nodeList, String key, String value) {

        for (RedisServer node : nodeList) {
            Jedis jedis = null;
            try {
                jedis = getClient(node);
                jedis.zrem(key, value);
            } catch (Exception e) {
                Logs.REDIS.error("redis srem exception:{},{},{}", key, value, node, e);
            } finally {
                // 返还到连接池
                close(jedis);
            }
        }

    }

    /**
     * 从列表中获取指定返回的元素 start 和 end
     * 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public static <T> List<T> zrange(RedisServer node, String key, int start, int end, Class<T> clazz) {
        Set<String> value = null;
        Jedis jedis = null;
        try {
            jedis = getClient(node);
            value = jedis.zrange(key, start, end);
        } catch (Exception e) {
            Logs.REDIS.error("redis zrange exception:{},{},{},{}", key, start, end, node, e);
        } finally {
            // 返还到连接池
            close(jedis);
        }
        if (value != null) {
            List<T> newValue = Lists.newArrayList();
            for (String temp : value) {
                newValue.add(Jsons.fromJson(temp, clazz));
            }
            return newValue;
        }
        return null;
    }

}
