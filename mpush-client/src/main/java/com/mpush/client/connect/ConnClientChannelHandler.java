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

package com.mpush.client.connect;


import com.google.common.collect.Maps;
import com.mpush.api.Constants;
import com.mpush.api.connection.Connection;
import com.mpush.api.event.ConnectionCloseEvent;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.cache.redis.RedisKey;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.common.message.*;
import com.mpush.common.security.AesCipher;
import com.mpush.common.security.CipherBox;
import com.mpush.netty.connection.NettyConnection;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.thread.NamedPoolThreadFactory;
import com.mpush.tools.thread.ThreadNames;
import io.netty.channel.*;
import io.netty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2015/12/19.
 *
 * @author ohun@live.cn
 */
public final class ConnClientChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnClientChannelHandler.class);
    private static final Timer HASHED_WHEEL_TIMER = new HashedWheelTimer(new NamedPoolThreadFactory(ThreadNames.T_CONN_TIMER));
    public static final AttributeKey<ClientConfig> CONFIG_KEY = AttributeKey.newInstance("clientConfig");
    public static final TestStatistics STATISTICS = new TestStatistics();

    private final Connection connection = new NettyConnection();
    private ClientConfig clientConfig;
    private boolean perfTest;

    public ConnClientChannelHandler() {
        perfTest = true;
    }

    public ConnClientChannelHandler(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        connection.updateLastReadTime();
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(packet.cmd);
            if (command == Command.HANDSHAKE) {
                int connectedNum = STATISTICS.connectedNum.incrementAndGet();
                connection.getSessionContext().changeCipher(new AesCipher(clientConfig.getClientKey(), clientConfig.getIv()));
                HandshakeOkMessage message = new HandshakeOkMessage(packet, connection);
                byte[] sessionKey = CipherBox.I.mixKey(clientConfig.getClientKey(), message.serverKey);
                connection.getSessionContext().changeCipher(new AesCipher(sessionKey, clientConfig.getIv()));
                startHeartBeat(message.heartbeat);
                LOGGER.info(">>> handshake success, clientConfig={}, connectedNum={}", clientConfig, connectedNum);
                bindUser(clientConfig);
                if (!perfTest) {
                    saveToRedisForFastConnection(clientConfig, message.sessionId, message.expireTime, sessionKey);
                }
            } else if (command == Command.FAST_CONNECT) {
                int connectedNum = STATISTICS.connectedNum.incrementAndGet();
                String cipherStr = clientConfig.getCipher();
                String[] cs = cipherStr.split(",");
                byte[] key = AesCipher.toArray(cs[0]);
                byte[] iv = AesCipher.toArray(cs[1]);
                connection.getSessionContext().changeCipher(new AesCipher(key, iv));

                FastConnectOkMessage message = new FastConnectOkMessage(packet, connection);
                startHeartBeat(message.heartbeat);
                bindUser(clientConfig);
                LOGGER.info(">>> fast connect success, clientConfig={}, connectedNum={}", clientConfig, connectedNum);
            } else if (command == Command.KICK) {
                KickUserMessage message = new KickUserMessage(packet, connection);
                LOGGER.error(">>> receive kick user userId={}, deviceId={}, message={},", clientConfig.getUserId(), clientConfig.getDeviceId(), message);
                ctx.close();
            } else if (command == Command.ERROR) {
                ErrorMessage errorMessage = new ErrorMessage(packet, connection);
                LOGGER.error(">>> receive an error packet=" + errorMessage);
            } else if (command == Command.PUSH) {
                int receivePushNum = STATISTICS.receivePushNum.incrementAndGet();

                PushMessage message = new PushMessage(packet, connection);
                LOGGER.info(">>> receive an push message, content={}, receivePushNum={}", new String(message.content, Constants.UTF_8), receivePushNum);

                if (message.needAck()) {
                    AckMessage.from(message).sendRaw();
                    LOGGER.info(">>> send ack success for sessionId={}", message.getSessionId());
                }

            } else if (command == Command.HEARTBEAT) {
                LOGGER.info(">>> receive a heartbeat pong...");
            } else if (command == Command.OK) {
                OkMessage okMessage = new OkMessage(packet, connection);
                int bindUserNum = STATISTICS.bindUserNum.get();
                if (okMessage.cmd == Command.BIND.cmd) {
                    bindUserNum = STATISTICS.bindUserNum.incrementAndGet();
                }

                LOGGER.info(">>> receive an success message={}, bindUserNum={}", okMessage, bindUserNum);

            } else if (command == Command.HTTP_PROXY) {
                HttpResponseMessage message = new HttpResponseMessage(packet, connection);
                LOGGER.info(">>> receive a http response, message={}, body={}",
                        message, message.body == null ? null : new String(message.body, Constants.UTF_8));
            }
        }


        LOGGER.debug("update currentTime:" + ctx.channel() + "," + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        connection.close();
        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int clientNum = STATISTICS.clientNum.incrementAndGet();
        LOGGER.info("client connect channel={}, clientNum={}", ctx.channel(), clientNum);
        if (clientConfig == null) {
            clientConfig = ctx.channel().attr(CONFIG_KEY).getAndRemove();
        }
        connection.init(ctx.channel(), true);
        if (perfTest) {
            handshake();
        } else {
            tryFastConnect();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int clientNum = STATISTICS.clientNum.decrementAndGet();
        connection.close();
        EventBus.I.post(new ConnectionCloseEvent(connection));
        LOGGER.info("client disconnect channel={}, clientNum={}", connection, clientNum);
    }

    private void tryFastConnect() {

        Map<String, String> sessionTickets = getFastConnectionInfo(clientConfig.getDeviceId());

        if (sessionTickets == null) {
            handshake();
            return;
        }
        String sessionId = sessionTickets.get("sessionId");
        if (sessionId == null) {
            handshake();
            return;
        }
        String expireTime = sessionTickets.get("expireTime");
        if (expireTime != null) {
            long exp = Long.parseLong(expireTime);
            if (exp < System.currentTimeMillis()) {
                handshake();
                return;
            }
        }

        final String cipher = sessionTickets.get("cipherStr");

        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = clientConfig.getDeviceId();
        message.sessionId = sessionId;

        message.sendRaw(channelFuture -> {
            if (channelFuture.isSuccess()) {
                clientConfig.setCipher(cipher);
            } else {
                handshake();
            }
        });
        LOGGER.debug("<<< send fast connect message={}", message);
    }

    private void bindUser(ClientConfig client) {
        BindUserMessage message = new BindUserMessage(connection);
        message.userId = client.getUserId();
        message.tags = "test";
        message.send();
        LOGGER.debug("<<< send bind user message={}", message);
    }

    private void saveToRedisForFastConnection(ClientConfig client, String sessionId, Long expireTime, byte[] sessionKey) {
        Map<String, String> map = Maps.newHashMap();
        map.put("sessionId", sessionId);
        map.put("expireTime", expireTime + "");
        map.put("cipherStr", connection.getSessionContext().cipher.toString());
        String key = RedisKey.getDeviceIdKey(client.getDeviceId());
        RedisManager.I.set(key, map, 60 * 5); //5分钟
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getFastConnectionInfo(String deviceId) {
        String key = RedisKey.getDeviceIdKey(deviceId);
        return RedisManager.I.get(key, Map.class);
    }

    private void handshake() {
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = clientConfig.getClientKey();
        message.iv = clientConfig.getIv();
        message.clientVersion = clientConfig.getClientVersion();
        message.deviceId = clientConfig.getDeviceId();
        message.osName = clientConfig.getOsName();
        message.osVersion = clientConfig.getOsVersion();
        message.timestamp = System.currentTimeMillis();
        message.send();
        LOGGER.debug("<<< send handshake message={}", message);
    }

    private void startHeartBeat(final int heartbeat) throws Exception {
        HASHED_WHEEL_TIMER.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                final TimerTask self = this;
                final Channel channel = connection.getChannel();
                if (channel.isActive()) {
                    ChannelFuture channelFuture = channel.writeAndFlush(Packet.getHBPacket());
                    channelFuture.addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            LOGGER.debug("<<< send heartbeat ping... " + channel.remoteAddress().toString());
                        } else {
                            LOGGER.warn("client send msg hb false:" + channel.remoteAddress().toString(), future.cause());
                        }
                        HASHED_WHEEL_TIMER.newTimeout(self, heartbeat, TimeUnit.MILLISECONDS);
                    });
                } else {
                    LOGGER.error("connection was closed, connection={}", connection);
                }
            }
        }, heartbeat, TimeUnit.MILLISECONDS);
    }
}