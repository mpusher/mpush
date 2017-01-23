/*
  * (C) Copyright 2015-2016 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Contributors:
  *     ohun@live.cn (夜色)
  */
package com.mpush.netty.http;

import com.google.common.collect.ArrayListMultimap;
import com.mpush.tools.config.CC;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yxx on 2016/5/28.
 *
 * @author ohun@live.cn (夜色)
 */
/*package*/ class HttpConnectionPool {
    private static final int maxConnPerHost = CC.mp.http.max_conn_per_host;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectionPool.class);

    private final AttributeKey<String> hostKey = AttributeKey.newInstance("host");

    private final ArrayListMultimap<String, Channel> channelPool = ArrayListMultimap.create();

    public synchronized Channel tryAcquire(String host) {
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.isEmpty()) return null;
        Iterator<Channel> it = channels.iterator();
        while (it.hasNext()) {
            Channel channel = it.next();
            it.remove();
            if (channel.isActive()) {
                LOGGER.debug("tryAcquire channel success, host={}", host);
                channel.attr(hostKey).set(host);
                return channel;
            } else {//链接由于意外情况不可用了, 比如: keepAlive_timeout
                LOGGER.warn("tryAcquire channel false channel is inactive, host={}", host);
            }
        }
        return null;
    }

    public synchronized void tryRelease(Channel channel) {
        String host = channel.attr(hostKey).getAndSet(null);
        List<Channel> channels = channelPool.get(host);
        if (channels == null || channels.size() < maxConnPerHost) {
            LOGGER.debug("tryRelease channel success, host={}", host);
            channelPool.put(host, channel);
        } else {
            LOGGER.debug("tryRelease channel pool size over limit={}, host={}, channel closed.", maxConnPerHost, host);
            channel.close();
        }
    }

    public void attachHost(String host, Channel channel) {
        channel.attr(hostKey).set(host);
    }

    public void close() {
        channelPool.values().forEach(Channel::close);
        channelPool.clear();
    }
}
