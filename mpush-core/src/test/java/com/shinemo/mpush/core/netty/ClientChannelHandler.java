package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.message.*;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.security.AesCipher;
import com.shinemo.mpush.common.security.CipherBox;
import com.shinemo.mpush.netty.connection.NettyConnection;
import com.shinemo.mpush.netty.util.NettySharedHolder;
import com.shinemo.mpush.tools.Strings;

import io.netty.channel.*;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    private String deviceId = "test-device-id-100" + new Random(5).nextInt();
    private String userId = "1010";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection.init(ctx.channel(), true);
        HandshakeMessage message = new HandshakeMessage(connection);
        message.clientKey = clientKey;
        message.iv = iv;
        message.clientVersion = "1.0.1";
        message.deviceId = deviceId;
        message.osName = "android";
        message.osVersion = "5.0";
        message.timestamp = System.currentTimeMillis();
        message.send();
        LOGGER.info("client channel Active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client channel Inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("client read new packet=" + msg);
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(packet.cmd);
            if (command == Command.HANDSHAKE) {
                connection.getSessionContext().changeCipher(new AesCipher(clientKey, iv));
                HandshakeOkMessage message = new HandshakeOkMessage(packet, connection);
                byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, message.serverKey);
                saveToken(message.sessionId);
                connection.getSessionContext().changeCipher(new AesCipher(sessionKey, iv));
                startHeartBeat(message.heartbeat, ctx.channel());
                LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, message.serverKey);
                bindUser();
            } else if (command == Command.FAST_CONNECT) {
                LOGGER.info("fast connect success, packet=" + packet.getStringBody());
            } else if (command == Command.KICK) {
                LOGGER.error("receive kick user message=" + new KickUserMessage(packet, connection));
                ctx.close();
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
                                LOGGER.warn("client send hb failed:" + channel.remoteAddress().toString() + ",channel is not active");
                            } else {
                                LOGGER.warn("client send  hb failed:" + channel.remoteAddress().toString());
                            }
                        } else {
                            LOGGER.warn("client send  hb success:" + channel.remoteAddress().toString());
                        }
                    }
                });
                if (channel.isActive()) {
                    NettySharedHolder.HASHED_WHEEL_TIMER.newTimeout(this, heartbeat, TimeUnit.MILLISECONDS);
                }
            }
        }, heartbeat, TimeUnit.MILLISECONDS);
    }


    private void saveToken(String token) {
        try {
            String path = this.getClass().getResource("/").getFile();
            FileOutputStream out = new FileOutputStream(new File(path, "token.dat"));
            out.write(token.getBytes());
            out.close();
        } catch (Exception e) {
        }
    }

    private String getToken() {
        try {
            InputStream in = this.getClass().getResourceAsStream("/token.dat");
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return new String(bytes);
        } catch (Exception e) {
        }
        return Strings.EMPTY;
    }
}
