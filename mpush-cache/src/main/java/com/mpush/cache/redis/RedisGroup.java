package com.mpush.cache.redis;

import com.google.common.collect.Lists;

import java.util.List;


/**
 * redis 组
 */
public class RedisGroup {
    private List<RedisServer> redisServerList = Lists.newArrayList();

    public List<RedisServer> getRedisServerList() {
        return redisServerList;
    }

    public void setRedisServerList(List<RedisServer> redisServerList) {
        this.redisServerList = redisServerList;
    }

    public void addRedisNode(RedisServer node) {
        redisServerList.add(node);
    }

    public void remove(int i) {
        if (redisServerList != null) {
            redisServerList.remove(i);
        }
    }

    public void clear() {
        if (redisServerList != null) {
            redisServerList.clear();
        }
    }

    public RedisServer get(String key) {
        int i = key.hashCode() % redisServerList.size();
        return redisServerList.get(i);
    }

    public static RedisGroup from(com.mpush.tools.config.data.RedisGroup node) {
        RedisGroup group = new RedisGroup();
        for (com.mpush.tools.config.data.RedisServer rs : node.redisNodeList) {
            group.addRedisNode(new RedisServer(rs.getHost(), rs.getPort(), rs.getPassword()));
        }
        return group;
    }
}
