package com.mpush.cache.redis.manager;

import com.mpush.cache.redis.RedisGroup;
import com.mpush.cache.redis.RedisServer;

import java.util.List;

public interface RedisClusterManager {

    void init();

    List<RedisGroup> getGroupList();

    RedisServer randomGetRedisNode(String key);

    List<RedisServer> hashSet(String key);
}
