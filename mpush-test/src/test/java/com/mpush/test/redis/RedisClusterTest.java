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
 */

package com.mpush.test.redis;

import com.mpush.tools.Jsons;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
        cluster = new JedisCluster(jedisClusterNodes, new GenericObjectPoolConfig());
    }

    @Test
    public void test() {

        User user = new User("huang", 18, new Date());
        cluster.set("huang", Jsons.toJson(user));
        String ret = cluster.get("huang");
        User newUser = Jsons.fromJson(ret, User.class);
        System.out.println(ToStringBuilder.reflectionToString(newUser, ToStringStyle.JSON_STYLE));

    }

}
