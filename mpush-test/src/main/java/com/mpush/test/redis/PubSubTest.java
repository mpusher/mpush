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

import com.mpush.cache.redis.mq.ListenerDispatcher;
import com.mpush.cache.redis.manager.RedisManager;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

public class PubSubTest {
    ListenerDispatcher listenerDispatcher = new ListenerDispatcher();
    @Before
    public void init() {
    }

    @Test
    public void subpubTest() {
        
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/123");
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/124");
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
    }

    @Test
    public void pubsubTest() {
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/123");
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/124");
    }

    @Test
    public void pubTest() {
        RedisManager.I.publish("/hello/123", "123");
        RedisManager.I.publish("/hello/124", "124");
    }

    @Test
    public void subTest() {
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/123");
        RedisManager.I.subscribe(listenerDispatcher.getSubscriber(), "/hello/124");
        LockSupport.park();
    }

}
