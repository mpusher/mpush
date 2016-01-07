package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.common.message.*;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.security.AesCipher;
import com.shinemo.mpush.common.security.CipherBox;
import com.shinemo.mpush.netty.connection.NettyConnection;
import com.shinemo.mpush.netty.util.NettySharedHolder;
import com.shinemo.mpush.tools.Jsons;

import io.netty.channel.*;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientChannelHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);
    private byte[] clientKey = CipherBox.INSTANCE.randomAESKey();
    private byte[] iv = CipherBox.INSTANCE.randomAESIV();
    private Connection connection = new NettyConnection();
    private String deviceId;
    private String userId;
    private Map<String, Serializable> sessionTickets;

    public ClientChannelHandler() {
        Map<String, Serializable> map = getToken();
        if (map != null && map.size() > 0) {
            userId = (String) map.get("userId");
            deviceId = (String) map.get("deviceId");
            sessionTickets = map;
        }
        if (deviceId == null) {
            deviceId = "test-device-id-100" + new Random().nextInt(5);
        }
        if (userId == null) {
            userId = "user-" + new Random().nextInt(5);
        }
    }

    private void handshake() {
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = clientKey;
        message.iv = iv;
        message.clientVersion = "1.0.1";
        message.deviceId = deviceId;
        message.osName = "android";
        message.osVersion = "5.0";
        message.timestamp = System.currentTimeMillis();
        message.send();
    }

    private void tryFastConnect() {
        if (sessionTickets == null) {
            handshake();
            return;
        }
        String sessionId = (String) sessionTickets.get("sessionId");
        if (sessionId == null) {
            handshake();
            return;
        }
        String expireTime = (String) sessionTickets.get("expireTime");
        if (expireTime != null) {
            long exp = Long.parseLong(expireTime);
            if (exp < System.currentTimeMillis()) {
                handshake();
                return;
            }
        }
        FastConnectMessage message = new FastConnectMessage(connection);
        message.deviceId = deviceId;
        message.sessionId = sessionId;
        message.send(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    handshake();
                }
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client channel Active");
        connection.init(ctx.channel(), true);
        tryFastConnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client channel Inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //LOGGER.info("client read new packet=" + msg);
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(packet.cmd);
            if (command == Command.HANDSHAKE) {
                connection.getSessionContext().changeCipher(new AesCipher(clientKey, iv));
                HandshakeOkMessage message = new HandshakeOkMessage(packet, connection);
                byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, message.serverKey);
                connection.getSessionContext().changeCipher(new AesCipher(sessionKey, iv));
                startHeartBeat(message.heartbeat, ctx.channel());
                LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, message.serverKey);
                saveToken(message, connection.getSessionContext());
                bindUser();
            } else if (command == Command.FAST_CONNECT) {
                String cipherStr = (String) sessionTickets.get("cipher");
                String[] cs = cipherStr.split(",");
                byte[] key = AesCipher.toArray(cs[0]);
                byte[] iv = AesCipher.toArray(cs[1]);
                connection.getSessionContext().changeCipher(new AesCipher(key, iv));

                FastConnectOkMessage message = new FastConnectOkMessage(packet, connection);
                startHeartBeat(message.heartbeat, ctx.channel());
                bindUser();
                LOGGER.info("fast connect success, message=" + message);
            } else if (command == Command.KICK) {
                KickUserMessage message = new KickUserMessage(packet, connection);
                LOGGER.error("receive kick user userId={},deviceId={}, message={},", userId);
                if (!message.deviceId.equals(deviceId)) {
                    ctx.close();
                }
            } else if (command == Command.ERROR) {
                ErrorMessage errorMessage = new ErrorMessage(packet, connection);
                LOGGER.error("receive an error packet=" + errorMessage);
            } else if (command == Command.BIND) {
                OkMessage okMessage = new OkMessage(packet, connection);
                LOGGER.info("receive an success packet=" + okMessage);
            }
        }
    }

    private void bindUser() {
        BindUserMessage message = new BindUserMessage(connection);
        message.userId = userId;
        message.send();
    }

    public void startHeartBeat(final int heartbeat, final Channel channel) throws Exception {
        NettySharedHolder.HASHED_WHEEL_TIMER.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ChannelFuture channelFuture = channel.writeAndFlush(Packet.getHBPacket());
                channelFuture.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            if (!channel.isActive()) {
                                LOGGER.warn("client send hb failed:" + channel + ",channel is not active");
                            } else {
                                LOGGER.warn("client send  hb failed:" + channel);
                            }
                        } else {
                            //LOGGER.debug("client send  hb success:" + channel);
                        }
                    }
                });
                if (channel.isActive()) {
                    NettySharedHolder.HASHED_WHEEL_TIMER.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
                }
            }
        }, heartbeat, TimeUnit.MILLISECONDS);
    }


    private void saveToken(HandshakeOkMessage message, SessionContext context) {
        try {
            Map<String, Serializable> map = new HashMap<>();
            map.put("sessionId", message.sessionId);
            map.put("serverHost", message.serverHost);
            map.put("expireTime", Long.toString(message.expireTime));
            map.put("cipher", context.cipher.toString());
            map.put("deviceId", deviceId);
            map.put("userId", userId);
            String path = this.getClass().getResource("/").getFile();
            FileOutputStream out = new FileOutputStream(new File(path, "token.dat"));
            out.write(Jsons.toJson(map).getBytes(Constants.UTF_8));
            out.close();
        } catch (Exception e) {
        }
    }

    private Map<String, Serializable> getToken() {
        if (true) return Collections.EMPTY_MAP;
        try {
            InputStream in = this.getClass().getResourceAsStream("/token.dat");
            byte[] bytes = new byte[in.available()];
            if (bytes.length > 0) {
                in.read(bytes);
                Map<String, Serializable> map = Jsons.fromJson(bytes, Map.class);
                return map;
            }
            in.close();
        } catch (Exception e) {
        }
        return Collections.EMPTY_MAP;
    }

    public String getLastServerHost() {
        return sessionTickets == null ? null : (String) sessionTickets.get("serverHost");
    }
}
