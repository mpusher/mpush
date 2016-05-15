package com.mpush.tools.redis;

import com.mpush.tools.spi.SPI;

import java.util.List;

@SPI("redisRegister")
public interface RedisRegister {

    void init(List<RedisGroup> group);

    List<RedisGroup> getGroupList();

    RedisNode randomGetRedisNode(String key);

    List<RedisNode> hashSet(String key);
}
