package com.mpush.cache.redis;

/**
 * redis 相关的配置信息
 */
public class RedisServer extends com.mpush.tools.config.data.RedisServer {

    public RedisServer(String ip, int port, String password) {
        super(ip, port, password);
    }
}
