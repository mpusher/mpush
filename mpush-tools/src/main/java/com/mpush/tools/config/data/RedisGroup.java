package com.mpush.tools.config.data;

import java.util.Collections;
import java.util.List;


/**
 * redis 组
 */
public class RedisGroup {
    public List<RedisServer> redisNodeList = Collections.emptyList();

    public RedisGroup() {
    }

    public RedisGroup(List<RedisServer> redisNodeList) {
        this.redisNodeList = redisNodeList;
    }

    @Override
    public String toString() {
        return "RedisGroup{" +
                "redisNodeList=" + redisNodeList +
                '}';
    }
}
