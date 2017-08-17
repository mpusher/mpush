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
import com.mpush.api.service.BaseService;
import com.mpush.api.srd.ServiceListener;
import com.mpush.client.MPushClient;
import com.mpush.common.message.BaseMessage;
import com.mpush.tools.config.CC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by yxx on 2016/5/17.
 *
 * @author ohun@live.cn
 */
public abstract class GatewayConnectionFactory extends BaseService implements ServiceListener {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static GatewayConnectionFactory create(MPushClient mPushClient) {
        return CC.mp.net.udpGateway() ? new GatewayUDPConnectionFactory(mPushClient) : new GatewayTCPConnectionFactory(mPushClient);
    }

    abstract public Connection getConnection(String hostAndPort);

    abstract public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender);

    abstract public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender);

}
