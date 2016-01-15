package com.shinemo.mpush.tools.redis;

import java.util.List;

import com.shinemo.mpush.tools.spi.SPI;

@SPI("redisRegister")
public interface RedisRegister {

	public void init(List<RedisGroup> group);

	public List<RedisGroup> getGroupList();

	public RedisNode randomGetRedisNode(String key);

	public List<RedisNode> hashSet(String key);

}
