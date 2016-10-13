/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 *//*


package com.mpush.test.redis;

import com.google.common.collect.Lists;
import com.mpush.cache.redis.client.RedisClient;
import com.mpush.cache.redis.RedisServer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RedisUtilTest {


    RedisServer node = new RedisServer("127.0.0.1", 6379);

    List<RedisServer> nodeList = Lists.newArrayList(node);

    @Test
    public void testAddAndGetAndDelete() {
        Jedis jedis = RedisClient.getClient(node);
        jedis.set("hi", "huang");

        String ret = jedis.get("hi");
        System.out.println(ret);

        jedis.del("hi");
        ret = jedis.get("hi");
        if (ret == null) {
            System.out.println("ret is null");
        } else {
            System.out.println("ret is not null:" + ret);
        }

    }

    @Test
    public void testJedisPool() {
        // 最大连接数是8，因此，获取10个链接会抛错误
        List<Jedis> jedisList = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Jedis jedis = RedisClient.getClient(node);
            jedisList.add(jedis);
        }
    }

    @Test
    public void testJedisPool2() {
        // 最大连接数是8，因此，获取10个链接会抛错误
        List<Jedis> jedisList = Lists.newArrayList();
        for (int i = 1; i <= 8; i++) {
            Jedis jedis = RedisClient.getClient(node);
            jedisList.add(jedis);
        }

        System.out.println(jedisList.size());

        try {
            Jedis jedis = RedisClient.getClient(node);
            jedisList.add(jedis);
            System.out.println("first get jedis success");
        } catch (Exception e) {
            System.out.println(e);
        }

        // 关闭一个链接
        RedisClient.close(jedisList.get(0));

        try {
            Jedis jedis = RedisClient.getClient(node);
            jedisList.add(jedis);
            System.out.println("second get jedis success");
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(jedisList.size());
    }

    @Test
    public void testKV() {
        User user = new User("huang", 18, new Date());
        RedisClient.set(nodeList, "test", user);

        User nowUser = RedisClient.get(node, "test", User.class);
        System.out.println("node1:" + ToStringBuilder.reflectionToString(nowUser));

        nowUser = RedisClient.get(node, "test", User.class);
        System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));

        RedisClient.del(nodeList, "test");

        nowUser = RedisClient.get(node, "test", User.class);
        if (nowUser == null) {
            System.out.println("node2 nowUser is null");
        } else {
            System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));
        }

        nowUser = RedisClient.get(node, "test", User.class);
        if (nowUser == null) {
            System.out.println("node nowUser is null");
        } else {
            System.out.println("node:" + ToStringBuilder.reflectionToString(nowUser));
        }

        RedisClient.set(nodeList, "test", user, 10);

        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nowUser = RedisClient.get(node, "test", User.class);
        if (nowUser == null) {
            System.out.println("node2 nowUser is null");
        } else {
            System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));
        }

        nowUser = RedisClient.get(node, "test", User.class);
        if (nowUser == null) {
            System.out.println("node nowUser is null");
        } else {
            System.out.println("node:" + ToStringBuilder.reflectionToString(nowUser));
        }

    }

    @Test
    public void hashTest() {

        User user = new User("huang", 18, new Date());

        RedisClient.hset(nodeList, "hashhuang", "hi", user);

        User nowUser = RedisClient.hget(node, "hashhuang", "hi", User.class);
        System.out.println("node1:" + ToStringBuilder.reflectionToString(nowUser));

        nowUser = RedisClient.hget(node, "hashhuang", "hi", User.class);
        System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));

        Map<String, User> ret = RedisClient.hgetAll(node, "hashhuang", User.class);
        Iterator<Map.Entry<String, User>> iterator = ret.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, User> entry = iterator.next();
            String key = entry.getKey();
            User val = entry.getValue();
            System.out.println("all:" + key + "," + ToStringBuilder.reflectionToString(val));
        }

        RedisClient.hdel(nodeList, "hashhuang", "hi");

        nowUser = RedisClient.hget(node, "hashhuang", "hi", User.class);
        if (nowUser == null) {
            System.out.println("node2 nowUser is null");
        } else {
            System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));
        }

        nowUser = RedisClient.hget(node, "hashhuang", "hi", User.class);
        if (nowUser == null) {
            System.out.println("node nowUser is null");
        } else {
            System.out.println("node:" + ToStringBuilder.reflectionToString(nowUser));
        }

    }

    @Test
    public void testSet() {
//		System.out.println(RedisClient.sCard(node, RedisKey.getOnlineUserListKey()));

//		List<String> onlineUserIdList = RedisClient.sScan(node, RedisKey.getOnlineUserListKey(), String.class, 0);
//		System.out.println(onlineUserIdList.size());

    }

    @Test
    public void testlist() {
//		RedisClient.del(nodeList, RedisKey.getUserOfflineKey());
    }

    @Test
    public void testsortedset() {
//		RedisClient.zAdd(nodeList, RedisKey.getOnlineUserListKey(), "doctor1test");

//		long len =RedisClient.zCard(node, RedisKey.getOnlineUserListKey());
//		System.out.println(len);
    }

}
*/
