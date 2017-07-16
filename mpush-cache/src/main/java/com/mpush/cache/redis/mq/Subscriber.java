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

package com.mpush.cache.redis.mq;

import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;
import redis.clients.jedis.JedisPubSub;

public final class Subscriber extends JedisPubSub {
    private final ListenerDispatcher listenerDispatcher;

    public Subscriber(ListenerDispatcher listenerDispatcher) {
        this.listenerDispatcher = listenerDispatcher;
    }

    @Override
    public void onMessage(String channel, String message) {
        Logs.CACHE.info("onMessage:{},{}", channel, message);
        listenerDispatcher.onMessage(channel, message);
        super.onMessage(channel, message);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        Logs.CACHE.info("onPMessage:{},{},{}", pattern, channel, message);
        super.onPMessage(pattern, channel, message);
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        Logs.CACHE.info("onPSubscribe:{},{}", pattern, subscribedChannels);
        super.onPSubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        Logs.CACHE.info("onPUnsubscribe:{},{}", pattern, subscribedChannels);
        super.onPUnsubscribe(pattern, subscribedChannels);
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        Logs.CACHE.info("onSubscribe:{},{}", channel, subscribedChannels);
        super.onSubscribe(channel, subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        Logs.CACHE.info("onUnsubscribe:{},{}", channel, subscribedChannels);
        super.onUnsubscribe(channel, subscribedChannels);
    }


    @Override
    public void unsubscribe() {
        Logs.CACHE.info("unsubscribe");
        super.unsubscribe();
    }

    @Override
    public void unsubscribe(String... channels) {
        Logs.CACHE.info("unsubscribe:{}", Jsons.toJson(channels));
        super.unsubscribe(channels);
    }

}
