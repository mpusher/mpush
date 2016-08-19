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

    public static final ListenerDispatcher I = new ListenerDispatcher();

    private final Map<String, List<MessageListener>> subscribes = Maps.newTreeMap();

    private final Executor executor = ThreadPoolManager.I.getRedisExecutor();

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
        List<MessageListener> listeners = subscribes.get(channel);
        if (listeners == null) {
            listeners = Lists.newArrayList();
            subscribes.put(channel, listeners);
        }
        listeners.add(listener);
        RedisManager.I.subscribe(Subscriber.holder, channel);
    }
}
