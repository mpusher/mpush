package com.shinemo.mpush.tools.redis;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

import com.shinemo.mpush.tools.Jsons;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisClusterTest {

	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	
	JedisCluster cluster = null;

	@Before
	public void init() {
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7000));
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7001));
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7002));
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7003));
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7004));
		jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7005));
		cluster = new JedisCluster(jedisClusterNodes,RedisPoolConfig.config);
	}

	@Test
	public void test() {
		
		User user = new User("huang", 18, new Date());
		cluster.set("huang", Jsons.toJson(user));
		String  ret = cluster.get("huang");
		User newUser = Jsons.fromJson(ret, User.class);
		System.out.println(ToStringBuilder.reflectionToString(newUser, ToStringStyle.JSON_STYLE));
		
	}

}
