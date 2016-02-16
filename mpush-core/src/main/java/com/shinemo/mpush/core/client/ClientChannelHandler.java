package com.shinemo.mpush.core.client;


import java.util.Map;

import com.google.common.collect.Maps;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.Client;
import com.shinemo.mpush.api.RedisKey;
import com.shinemo.mpush.common.message.*;
import com.shinemo.mpush.common.security.AesCipher;
import com.shinemo.mpush.common.security.CipherBox;
import com.shinemo.mpush.netty.client.ChannelClientHandler;
import com.shinemo.mpush.netty.client.NettyClient;
import com.shinemo.mpush.netty.client.NettyClientFactory;
import com.shinemo.mpush.netty.client.SecurityNettyClient;
import com.shinemo.mpush.netty.connection.NettyConnection;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/19.
 */
@ChannelHandler.Sharable
public final class ClientChannelHandler extends ChannelHandlerAdapter implements ChannelClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);

    private Client client;

    public ClientChannelHandler(Client client) {
        this.client = client;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        client.getConnection().updateLastReadTime();
        if (client instanceof SecurityNettyClient) {
            SecurityNettyClient securityNettyClient = (SecurityNettyClient) client;
            Connection connection = client.getConnection();
            //加密
            if (msg instanceof Packet) {
                Packet packet = (Packet) msg;
                Command command = Command.toCMD(packet.cmd);
                if (command == Command.HANDSHAKE) {
                    connection.getSessionContext().changeCipher(new AesCipher(securityNettyClient.getClientKey(), securityNettyClient.getIv()));
                    HandshakeOkMessage message = new HandshakeOkMessage(packet, connection);
                    byte[] sessionKey = CipherBox.INSTANCE.mixKey(securityNettyClient.getClientKey(), message.serverKey);
                    connection.getSessionContext().changeCipher(new AesCipher(sessionKey, securityNettyClient.getIv()));
                    client.startHeartBeat(message.heartbeat);
                    LOGGER.info("会话密钥：{}，message={}", sessionKey, message);
                    bindUser(securityNettyClient);
                    saveToRedisForFastConnection(securityNettyClient, message.sessionId, message.expireTime, sessionKey);
                } else if (command == Command.FAST_CONNECT) {
                    String cipherStr = securityNettyClient.getCipher();
                    String[] cs = cipherStr.split(",");
                    byte[] key = AesCipher.toArray(cs[0]);
                    byte[] iv = AesCipher.toArray(cs[1]);
                    connection.getSessionContext().changeCipher(new AesCipher(key, iv));

                    FastConnectOkMessage message = new FastConnectOkMessage(packet, connection);
                    client.startHeartBeat(message.heartbeat);
                    bindUser(securityNettyClient);
                    LOGGER.info("fast connect success, message=" + message);
                } else if (command == Command.KICK) {
                    KickUserMessage message = new KickUserMessage(packet, connection);
                    LOGGER.error("receive kick user userId={}, deviceId={}, message={},", securityNettyClient.getUserId(), securityNettyClient.getDeviceId(), message);
                    ctx.close();
                } else if (command == Command.ERROR) {
                    ErrorMessage errorMessage = new ErrorMessage(packet, connection);
                    LOGGER.error("receive an error packet=" + errorMessage);
                } else if (command == Command.BIND) {
                    OkMessage okMessage = new OkMessage(packet, connection);
                    LOGGER.info("receive an success packet=" + okMessage);
                    HttpRequestMessage message = new HttpRequestMessage("http://baidu.com", connection);
                    message.send();
                } else if (command == Command.PUSH) {
                    PushMessage message = new PushMessage(packet, connection);
                    LOGGER.info("receive an push message, content=" + message.content);
                } else if (command == Command.HEARTBEAT) {
                    LOGGER.info("receive a heartbeat pong...");
                } else {
                    LOGGER.info("receive a message, type=" + command + "," + packet);
                }
            }

        } else if (client instanceof NettyClient) {//不加密

        }
        LOGGER.warn("update currentTime:" + ctx.channel() + "," + ToStringBuilder.reflectionToString(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (client instanceof SecurityNettyClient) {
            NettyClientFactory.INSTANCE.remove(ctx.channel());
        } else {
            client.close("exception");
        }

        LOGGER.error("caught an ex, channel={}", ctx.channel(), cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client connect channel={}", ctx.channel());
        Connection connection = new NettyConnection();

        if (client instanceof SecurityNettyClient) {
            NettyClientFactory.INSTANCE.put(ctx.channel(), client);
            connection.init(ctx.channel(), true);
            client.initConnection(connection);
            client.init(ctx.channel());
            tryFastConnect((SecurityNettyClient) client);
        } else {
            LOGGER.error("connection is not support appear hear:" + client);
            connection.init(ctx.channel(), false);
            client.initConnection(connection);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (client instanceof SecurityNettyClient) {
            NettyClientFactory.INSTANCE.remove(ctx.channel());
        } else {
            client.close("inactive");
        }
        LOGGER.info("client disconnect channel={}", ctx.channel());
    }

    private void tryFastConnect(final SecurityNettyClient securityNettyClient) {

        Map<String, String> sessionTickets = getFastConnectionInfo(securityNettyClient.getDeviceId());

        if (sessionTickets == null) {
            handshake(securityNettyClient);
            return;
        }
        String sessionId = (String) sessionTickets.get("sessionId");
        if (sessionId == null) {
            handshake(securityNettyClient);
            return;
        }
        String expireTime = (String) sessionTickets.get("expireTime");
        if (expireTime != null) {
            long exp = Long.parseLong(expireTime);
            if (exp < System.currentTimeMillis()) {
                handshake(securityNettyClient);
                return;
            }
        }

        final String cipher = sessionTickets.get("cipherStr");

        FastConnectMessage message = new FastConnectMessage(securityNettyClient.getConnection());
        message.deviceId = securityNettyClient.getDeviceId();
        message.sessionId = sessionId;

        message.sendRaw(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    securityNettyClient.setCipher(cipher);
                } else {
                    handshake(securityNettyClient);
                }
            }
        });
    }

    private void bindUser(SecurityNettyClient client) {
        BindUserMessage message = new BindUserMessage(client.getConnection());
        message.userId = client.getUserId();
        message.send();
    }

    private void saveToRedisForFastConnection(SecurityNettyClient client, String sessionId, Long expireTime, byte[] sessionKey) {
        Map<String, String> map = Maps.newHashMap();
        map.put("sessionId", sessionId);
        map.put("expireTime", expireTime + "");
        map.put("cipherStr", client.getConnection().getSessionContext().cipher.toString());
        String key = RedisKey.getDeviceIdKey(client.getDeviceId());
        RedisManage.set(key, map, 60 * 5); //5分钟
    }

    private Map<String, String> getFastConnectionInfo(String deviceId) {
        String key = RedisKey.getDeviceIdKey(deviceId);
        return RedisManage.get(key, Map.class);
    }

    private void handshake(SecurityNettyClient client) {
        HandshakeMessage message = new HandshakeMessage(client.getConnection());
        message.clientKey = client.getClientKey();
        message.iv = client.getIv();
        message.clientVersion = client.getClientVersion();
        message.deviceId = client.getDeviceId();
        message.osName = client.getOsName();
        message.osVersion = client.getOsVersion();
        message.timestamp = System.currentTimeMillis();
        message.send();
    }

}