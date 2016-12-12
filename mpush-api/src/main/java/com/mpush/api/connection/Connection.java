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

package com.mpush.api.connection;

import com.mpush.api.protocol.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn (夜色)
 */
public interface Connection {
    byte STATUS_NEW = 0;
    byte STATUS_CONNECTED = 1;
    byte STATUS_DISCONNECTED = 2;

    void init(Channel channel, boolean security);

    SessionContext getSessionContext();

    void setSessionContext(SessionContext context);

    ChannelFuture send(Packet packet);

    ChannelFuture send(Packet packet, ChannelFutureListener listener);

    String getId();

    ChannelFuture close();

    boolean isConnected();

    boolean isReadTimeout();

    boolean isWriteTimeout();

    void updateLastReadTime();

    void updateLastWriteTime();

    Channel getChannel();

}
