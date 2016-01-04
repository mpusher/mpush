package com.shinemo.mpush.tools.redis;

import com.shinemo.mpush.tools.Constants;

import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolConfig {
	
	public static JedisPoolConfig config = new JedisPoolConfig();
	
	static{
		//连接池中最大连接数。高版本：maxTotal，低版本：maxActive
		config.setMaxTotal(Constants.REDIS_MAX_TOTAL);
		//连接池中最大空闲的连接数
		config.setMaxIdle(Constants.REDIS_MAX_IDLE);
		//连接池中最少空闲的连接数
		config.setMinIdle(Constants.REDIS_MIN_IDLE);
		//当连接池资源耗尽时，调用者最大阻塞的时间，超时将跑出异常。单位，毫秒数;默认为-1.表示永不超时。高版本：maxWaitMillis，低版本：maxWait
		config.setMaxWaitMillis(Constants.REDIS_MAX_WAITMILLIS);
		//连接空闲的最小时间，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除
		config.setMinEvictableIdleTimeMillis(Constants.REDIS_MIN_EVICTABLEIDLETIMEMILLIS);
		//对于“空闲链接”检测线程而言，每次检测的链接资源的个数。默认为3
		config.setNumTestsPerEvictionRun(Constants.REDIS_NUMTESTSPEREVICTIONRUN);
		//“空闲链接”检测线程，检测的周期，毫秒数。如果为负值，表示不运行“检测线程”。默认为-1
		config.setTimeBetweenEvictionRunsMillis(Constants.REDIS_TIMEBETWEENEVICTIONRUNMILLIS);
		//testOnBorrow:向调用者输出“链接”资源时，是否检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取。默认为false。建议保持默认值.
		config.setTestOnBorrow(Constants.REDIS_TESTONBORROW);
		//testOnReturn:向连接池“归还”链接时，是否检测“链接”对象的有效性。默认为false。建议保持默认值
		config.setTestOnReturn(Constants.REDIS_TESTONRETURN);
		//testWhileIdle:向调用者输出“链接”对象时，是否检测它的空闲超时；默认为false。如果“链接”空闲超时，将会被移除。建议保持默认值.
		config.setTestWhileIdle(Constants.REDIS_TESTWHILEIDLE);
	}
	

}
