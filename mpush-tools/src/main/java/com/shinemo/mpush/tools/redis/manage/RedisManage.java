package com.shinemo.mpush.tools.redis.manage;

import java.util.List;
import java.util.Map;
import java.util.Set;


import redis.clients.jedis.JedisPubSub;

import com.google.common.collect.Sets;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisNode;
import com.shinemo.mpush.tools.redis.RedisRegister;
import com.shinemo.mpush.tools.redis.RedisUtil;
import com.shinemo.mpush.tools.spi.ServiceContainer;

/**
 * redis 对外封装接口
 *
 */
public class RedisManage {
	
	private static final RedisRegister redisRegister = ServiceContainer.getInstance(RedisRegister.class);

	public static long incr(String key,Integer time){
		List<RedisNode> nodeList = redisRegister.hashSet(key);
		return RedisUtil.incr(nodeList, key, time);
	} 
	
    /*********************
     * k v redis start
     ********************************/

    public static <T> T get(String key, Class<T> clazz) {
        RedisNode node = redisRegister.randomGetRedisNode(key);
        return RedisUtil.get(node, key, clazz);
    }

    public static <T> void set(String key, T value) {
        set(key, value, null);
    }

    public static <T> void set(String key, T value, Integer time) {
        String jsonValue = Jsons.toJson(value);
        set(key, jsonValue, time);
    }

    /**
     * @param nodeList
     * @param key
     * @param value
     * @param time     seconds
     */
    public static void set(String key, String value, Integer time) {
        List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.set(nodeList, key, value, time);
    }

    public static void del(String key) {

        List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.del(nodeList, key);

    }

    /*********************k v redis end********************************/


    /*********************
     * hash redis start
     ********************************/
    public static void hset(String namespace, String key, String value) {

        List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.hset(nodeList, namespace, key, value);

    }

    public static <T> void hset(String namespace, String key, T value) {
        hset(namespace, key, Jsons.toJson(value));
    }

    public static <T> T hget(String namespace, String key, Class<T> clazz) {

        RedisNode node = redisRegister.randomGetRedisNode(namespace);
        return RedisUtil.hget(node, namespace, key, clazz);

    }

    public static void hdel(String namespace, String key) {
        List<RedisNode> nodeList = redisRegister.hashSet(namespace);
        RedisUtil.hdel(nodeList, namespace, key);
    }

    public static Map<String, String> hgetAll(String namespace) {

        RedisNode node = redisRegister.randomGetRedisNode(namespace);
        return RedisUtil.hgetAll(node, namespace);

    }

    public static <T> Map<String, T> hgetAll(String namespace, Class<T> clazz) {
        RedisNode node = redisRegister.randomGetRedisNode(namespace);
        return RedisUtil.hgetAll(node, namespace, clazz);
    }

    /**
     * 返回 key 指定的哈希集中所有字段的名字。
     *
     * @param node
     * @param key
     * @return
     */
    public static Set<String> hkeys(String namespace) {
        RedisNode node = redisRegister.randomGetRedisNode(namespace);
        return RedisUtil.hkeys(node, namespace);
    }

    /**
     * 返回 key 指定的哈希集中指定字段的值
     *
     * @param node
     * @param key
     * @param clazz
     * @param fields
     * @return
     */
    public static <T> List<T> hmget(String namespace, Class<T> clazz, String... key) {
        RedisNode node = redisRegister.randomGetRedisNode(namespace);
        return RedisUtil.hmget(node, namespace, clazz, key);
    }

    /**
     * 设置 key 指定的哈希集中指定字段的值。该命令将重写所有在哈希集中存在的字段。如果 key 指定的哈希集不存在，会创建一个新的哈希集并与 key 关联
     *
     * @param nodeList
     * @param key
     * @param hash
     * @param time
     */
    public static void hmset(String namespace, Map<String, String> hash, Integer time) {
        List<RedisNode> nodeList = redisRegister.hashSet(namespace);
        RedisUtil.hmset(nodeList, namespace, hash, time);
    }

    public static void hmset(String namespace, Map<String, String> hash) {
        hmset(namespace, hash, null);
    }


    /*********************hash redis end********************************/


    /*********************list redis start********************************/
    /**
     * 从队列的左边入队
     */
    public static void lpush(String key, String value) {
        List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.lpush(nodeList, key, value);
    }

    public static <T> void lpush(String key, T value) {
        lpush(key, Jsons.toJson(value));
    }

    /**
     * 从队列的右边入队
     */
    public static void rpush(String key, String value) {
        List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.rpush(nodeList, key, value);
    }

    public static <T> void rpush(String key, T value) {
        rpush(key, Jsons.toJson(value));
    }

    /**
     * 移除并且返回 key 对应的 list 的第一个元素
     */
    public static <T> T lpop(String key, Class<T> clazz) {
        List<RedisNode> nodeList = redisRegister.hashSet(key);
        return RedisUtil.lpop(nodeList, key, clazz);
    }

    /**
     * 从队列的右边出队一个元素
     */
    public static <T> T rpop(String key, Class<T> clazz) {
        List<RedisNode> nodeList = redisRegister.hashSet(key);
        return RedisUtil.rpop(nodeList, key, clazz);
    }


    /**
     * 从列表中获取指定返回的元素
     * start 和 end 偏移量都是基于0的下标，即list的第一个元素下标是0（list的表头），第二个元素下标是1，以此类推。
     * 偏移量也可以是负数，表示偏移量是从list尾部开始计数。 例如， -1 表示列表的最后一个元素，-2 是倒数第二个，以此类推。
     */
    public static <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        RedisNode node = redisRegister.randomGetRedisNode(key);
        return RedisUtil.lrange(node, key, start, end, clazz);
    }

    /**
     * 返回存储在 key 里的list的长度。 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。 当存储在 key 里的值不是一个list的话，会返回error。
     */
    public static long llen(String key) {
        RedisNode node = redisRegister.randomGetRedisNode(key);
        return RedisUtil.llen(node, key);
    }

    public static <T> void publish(String channel, T message) {

        RedisNode node = redisRegister.randomGetRedisNode(channel);
        RedisUtil.publish(node, channel, message);

    }

    public static void subscribe(JedisPubSub pubsub, String... channels) {

        Set<RedisNode> set = Sets.newHashSet();
        for (String channel : channels) {
            List<RedisNode> nodeList = redisRegister.hashSet(channel);
            set.addAll(nodeList);
        }

        RedisUtil.subscribe(set, pubsub, channels);
    }
    
    public static <T> void sAdd(String key, T value) {
    	 String jsonValue = Jsons.toJson(value);
    	 List<RedisNode> nodeList = redisRegister.hashSet(key);
         RedisUtil.sAdd(nodeList, key, jsonValue);
    }
    
    public static Long sCard(String key) {
    	RedisNode node = redisRegister.randomGetRedisNode(key);
        return RedisUtil.sCard(node, key);
    }
    
    public static <T> void sRem(String key, T value) {
   	    String jsonValue = Jsons.toJson(value);
   	    List<RedisNode> nodeList = redisRegister.hashSet(key);
        RedisUtil.sRem(nodeList, key, jsonValue);
    }
    
    public static <T> List<T> sScan(String key,int start, Class<T> clazz) {
    	RedisNode node = redisRegister.randomGetRedisNode(key);
        return RedisUtil.sScan(node, key, clazz, start);
    }
    
    
}
