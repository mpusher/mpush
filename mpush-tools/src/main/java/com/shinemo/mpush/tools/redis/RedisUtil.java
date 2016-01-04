package com.shinemo.mpush.tools.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.zk.manage.ServerAppManage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class RedisUtil {
	
	private static final Logger log = LoggerFactory.getLogger(ServerAppManage.class);
	
	private static Map<RedisNode,JedisPool> holder  =  Maps.newConcurrentMap();

	public static Jedis getClient(RedisNode node) {
		JedisPool pool = holder.get(node);
		if(pool == null){
			pool = new JedisPool(RedisPoolConfig.config, node.getIp(), node.getPort(), Constants.REDIS_TIMEOUT, node.getPassword());
			holder.put(node, pool);
		}
		return pool.getResource();
	}
	
	public static void close(Jedis jedis){
		jedis.close();
	}
	
    /*********************k v redis start********************************/
	/**
	 * 
	 * @param node  redis实例
	 * @param key
	 * @param clazz
	 * @return
	 */
	public  static <T> T get(RedisNode node,String key,Class<T> clazz) {

		String value = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			value = jedis.get(key);
		} catch (Exception e) {
			log.warn("redis get exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		return Jsons.fromJson(value, clazz);
	}
	
	public static void set(List<RedisNode> nodeList,String key,String value) {

		set(nodeList, key, value, null);
		
	}
	
	public static <T> void set(List<RedisNode> nodeList,String key,T value) {
		set(nodeList, key, value, null);
	}
	
	public static <T> void set(List<RedisNode> nodeList,String key,T value,Integer time) {
		String jsonValue = Jsons.toJson(value);
		set(nodeList, key, jsonValue, time);
	}
	
	/**
	 * 
	 * @param nodeList
	 * @param key
	 * @param value
	 * @param time seconds
	 */
	public static void set(List<RedisNode> nodeList,String key,String value,Integer time) {
		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.set(key, value);
				if(time!=null){
					jedis.expire(key, time);
				}
			} catch (Exception e) {
				log.warn("redis set exception:"+key+","+value+","+time,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
	}
	
	public static void del(List<RedisNode> nodeList,String key) {

		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.del(key);
			} catch (Exception e) {
				log.warn("redis del exception:"+key,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
		
	}
	
    /*********************k v redis end********************************/
	
	
    /*********************hash redis start********************************/
	public static void hset(List<RedisNode> nodeList,String key, String field, String value) {
		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.hset(key, field, value);
			} catch (Exception e) {
				log.warn("redis hset exception:"+key+","+field+","+value,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
	}
	
	public static <T> T hget(RedisNode node,String key, String field,Class<T> clazz) {

		String value = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			value = jedis.get(key);
		} catch (Exception e) {
			log.warn("redis hget exception:"+key+","+field,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		return Jsons.fromJson(value, clazz);
	}
	
	public static void hdel(List<RedisNode> nodeList,String key, String field) {

		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.hdel(key, field);
			} catch (Exception e) {
				log.warn("redis hdel exception:"+key+","+field,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
	}
	
	/**
	 * 存储REDIS队列 顺序存储
	 */
	public static void lpush(List<RedisNode> nodeList,String key, String value) {

		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.lpush(key, value);
			} catch (Exception e) {
				log.warn("redis hdel exception:"+key+","+value,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
		
	}
	
	/**
	 * 存储REDIS队列 反向存储
	 */
	public static void rpush(List<RedisNode> nodeList,String key, String value) {

		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.rpush(key, value);
			} catch (Exception e) {
				log.warn("redis hdel exception:"+key+","+value,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
	}
	
	/**
	 * 
	 */
	public static void rpoplpush(List<RedisNode> nodeList,String srcKey, String desKey) {

		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.rpoplpush(srcKey, desKey);
			} catch (Exception e) {
				log.warn("redis rpoplpush exception:"+srcKey+","+desKey,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
		
	}

	/**
	 * 获取队列数据
	 */
	public static <T> List<T> lpopList(RedisNode node,String key,Class<T> clazz) {
		List<String> value = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			value = jedis.lrange(key, 0, -1);
		} catch (Exception e) {
			log.warn("redis lpopList exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		if(value!=null){
			List<T> newValue = Lists.newArrayList();
			for(String temp:value){
				newValue.add(Jsons.fromJson(temp, clazz));
			}
			return newValue;
		}
		return null;
	}

	/**
	 * 获取队列数据
	 */
	public static <T> T rpop(RedisNode node,String key,Class<T> clazz) {

		Jedis jedis = null;
		String vaule = null;
		try {
			jedis = getClient(node);
			vaule = jedis.rpop(key);
		} catch (Exception e) {
			log.warn("redis rpop exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		return Jsons.fromJson(vaule, clazz);
	}

	public static void hmset(List<RedisNode> nodeList,String key, Map<String, String> hash,Integer time) {
		
		for(RedisNode node:nodeList){
			Jedis jedis = null;
			try {
				jedis = getClient(node);
				jedis.hmset(key, hash);
			} catch (Exception e) {
				log.warn("redis hmset exception:"+key,e);
			} finally {
				//返还到连接池
				close(jedis);
			}
		}
		
	}

	public static void hmset(List<RedisNode> nodeList,String key, Map<String, String> hash, int time) {
		hmset(nodeList, key, hash, null);
	}

	public static <T>  List<T> hmget(RedisNode node ,String key, Class<T> clazz,String... fields) {
		
		List<String> value = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			value = jedis.hmget(key.toString(), fields);
		} catch (Exception e) {
			log.warn("redis lpopList exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		if(value!=null){
			List<T> newValue = Lists.newArrayList();
			for(String temp:value){
				newValue.add(Jsons.fromJson(temp, clazz));
			}
			return newValue;
		}
		return null;
		
	}

	public static Set<String> hkeys(RedisNode node,String key) {
		Set<String> result = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			result = jedis.hkeys(key);
		} catch (Exception e) {
			log.warn("redis hkeys exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);

		}
		return result;
	}

	public static <T> List<T> lrange(RedisNode node,String key, Class<T> clazz ,int from, int to) {
		List<String> result = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			result = jedis.lrange(key, from, to);
		} catch (Exception e) {
			log.warn("redis lrange exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);

		}
		if(result!=null){
			List<T> newValue = Lists.newArrayList();
			for(String temp:result){
				newValue.add(Jsons.fromJson(temp, clazz));
			}
			return newValue;
		}
		return null;
	}

	public static Map<String, String> hgetAll(RedisNode node,String key) {
		Map<String, String> result = null;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			result = jedis.hgetAll(key);
		} catch (Exception e) {
			log.warn("redis hgetAll exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}

	

	public static long llen(RedisNode node ,String key) {

		long len = 0;
		Jedis jedis = null;
		try {
			jedis = getClient(node);
			jedis.llen(key);
		} catch (Exception e) {
			log.warn("redis llen exception:"+key,e);
		} finally {
			//返还到连接池
			close(jedis);
		}
		return len;
	}

}
