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

package com.mpush.client.gateway.connection;

import com.mpush.api.connection.Connection;
import com.mpush.api.service.Listener;
import com.mpush.common.message.BaseMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.zk.cache.ZKServerNodeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public abstract class GatewayConnectionFactory extends ZKServerNodeCache {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void init(Listener listener) {
    }

    abstract public Connection getConnection(String ip);

    abstract public <M extends BaseMessage> Function<String, Void> send(Function<Connection, M> creator, Function<M, Void> sender);

    abstract public <M extends BaseMessage> void broadcast(Function<Connection, M> creator, Consumer<M> sender);

}
