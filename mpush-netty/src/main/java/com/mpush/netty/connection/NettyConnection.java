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

package com.mpush.netty.connection;

import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.protocol.Packet;
import com.mpush.api.spi.core.RsaCipherFactory;
import com.mpush.tools.log.Logs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public final class NettyConnection implements Connection, ChannelFutureListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnection.class);
    private SessionContext context;
    private Channel channel;
    private volatile byte status = STATUS_NEW;
    private long lastReadTime;
    private long lastWriteTime;

    @Override
    public void init(Channel channel, boolean security) {
        this.channel = channel;
        this.context = new SessionContext();
        this.lastReadTime = System.currentTimeMillis();
        this.status = STATUS_CONNECTED;
        if (security) {
            this.context.changeCipher(RsaCipherFactory.create());
        }
    }

    @Override
    public void setSessionContext(SessionContext context) {
        this.context = context;
    }

    @Override
    public SessionContext getSessionContext() {
        return context;
    }

    @Override
    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public ChannelFuture send(Packet packet) {
        return send(packet, null);
    }

    @Override
    public ChannelFuture send(Packet packet, final ChannelFutureListener listener) {
        if (channel.isActive()) {

            ChannelFuture future = channel.writeAndFlush(packet.toFrame(channel)).addListener(this);

            if (listener != null) {
                future.addListener(listener);
            }

            if (channel.isWritable()) {
                return future;
            }

            //阻塞调用线程还是抛异常？
            //return channel.newPromise().setFailure(new RuntimeException("send data too busy"));
            if (!future.channel().eventLoop().inEventLoop()) {
                future.awaitUninterruptibly(100);
            }
            return future;
        } else {
            /*if (listener != null) {
                channel.newPromise()
                        .addListener(listener)
                        .setFailure(new RuntimeException("connection is disconnected"));
            }*/
            return this.close();
        }
    }

    @Override
    public ChannelFuture close() {
        if (status == STATUS_DISCONNECTED) return null;
        this.status = STATUS_DISCONNECTED;
        return this.channel.close();
    }

    @Override
    public boolean isConnected() {
        return status == STATUS_CONNECTED;
    }

    @Override
    public boolean isReadTimeout() {
        return System.currentTimeMillis() - lastReadTime > context.heartbeat + 1000;
    }

    @Override
    public boolean isWriteTimeout() {
        return System.currentTimeMillis() - lastWriteTime > context.heartbeat - 1000;
    }

    @Override
    public void updateLastReadTime() {
        lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            lastWriteTime = System.currentTimeMillis();
        } else {
            LOGGER.error("connection send msg error", future.cause());
            Logs.CONN.error("connection send msg error={}, conn={}", future.cause().getMessage(), this);
        }
    }

    @Override
    public void updateLastWriteTime() {
        lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "[channel=" + channel
                + ", context=" + context
                + ", status=" + status
                + ", lastReadTime=" + lastReadTime
                + ", lastWriteTime=" + lastWriteTime
                + "]";
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NettyConnection that = (NettyConnection) o;

        return channel.id().equals(that.channel.id());
    }

    @Override
    public int hashCode() {
        return channel.id().hashCode();
    }
}
