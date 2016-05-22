package com.mpush.test.redis;

import com.mpush.cache.redis.RedisGroup;
import com.mpush.cache.redis.RedisServer;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.cache.redis.manager.ZKRedisClusterManager;
import com.mpush.cache.redis.mq.Subscriber;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

public class PubSubTest {

    private ZKRedisClusterManager redisClusterManager = ZKRedisClusterManager.I;

    @Before
    public void init() {
        RedisServer node = new RedisServer("127.0.0.1", 6379, "shinemoIpo");
        RedisGroup group = new RedisGroup();
        group.addRedisNode(node);
        redisClusterManager.addGroup(group);
    }

    @Test
    public void subpubTest() {
        RedisManager.I.subscribe(Subscriber.holder, "/hello/123");
        RedisManager.I.subscribe(Subscriber.holder, "/hello/124");
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
    }

    @Test
    public void pubsubTest() {
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
        RedisManager.I.subscribe(Subscriber.holder, "/hello/123");
        RedisManager.I.subscribe(Subscriber.holder, "/hello/124");
    }

    @Test
    public void pubTest() {
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
    }

    @Test
    public void subTest() {
        RedisManager.I.subscribe(Subscriber.holder, "/hello/123");
        RedisManager.I.subscribe(Subscriber.holder, "/hello/124");
        LockSupport.park();
    }

}
