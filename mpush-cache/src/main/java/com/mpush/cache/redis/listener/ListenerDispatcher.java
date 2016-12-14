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

package com.mpush.cache.redis.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.cache.redis.mq.Subscriber;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.pool.ThreadPoolManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class ListenerDispatcher implements MessageListener {

    private static ListenerDispatcher I;

    private final Map<String, List<MessageListener>> subscribes = Maps.newTreeMap();

    private final Executor executor = ThreadPoolManager.I.getRedisExecutor();

    private final Subscriber subscriber = new Subscriber();


    public static ListenerDispatcher I() {
        if (I == null) {
            synchronized (ListenerDispatcher.class) {
                if (I == null) {
                    I = new ListenerDispatcher();
                }
            }
        }
        return I;
    }

    private ListenerDispatcher() {
    }

    @Override
    public void onMessage(final String channel, final String message) {
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
            Logs.REDIS.info("cannot find listener:%s,%s", channel, message);
            return;
        }
        for (final MessageListener listener : listeners) {
            executor.execute(() -> listener.onMessage(channel, message));
        }
    }

    public void subscribe(String channel, MessageListener listener) {
        subscribes.computeIfAbsent(channel, k -> Lists.newArrayList()).add(listener);
        RedisManager.I.subscribe(subscriber, channel);
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }
}
