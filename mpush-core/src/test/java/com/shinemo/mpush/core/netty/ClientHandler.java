package com.shinemo.mpush.core.netty;

import com.shinemo.mpush.api.Connection;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Handler;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.NettyConnection;
import com.shinemo.mpush.core.message.HandShakeMessage;
import com.shinemo.mpush.core.message.HandshakeSuccessMessage;
import com.shinemo.mpush.core.security.AesCipher;
import com.shinemo.mpush.core.security.CipherBox;
import com.shinemo.mpush.tools.Strings;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketAddress;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientHandler implements Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private byte[] clientKey = CipherBox.INSTANCE.randomAESKey();
    private byte[] iv = CipherBox.INSTANCE.randomAESIV();
    private Connection connection = new NettyConnection();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connection.init(ctx.channel());
        HandShakeMessage message = new HandShakeMessage(1, connection);
        message.clientKey = clientKey;
        message.iv = iv;
        message.clientVersion = "1.0.1";
        message.deviceId = "1111111111111";
        message.osName = "android";
        message.osVersion = "5.0";
        message.timestamp = System.currentTimeMillis();
        message.send();
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelInactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelRead", msg);
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(packet.cmd);
            if (command == Command.HANDSHAKE) {
                connection.getSessionContext().changeCipher(new AesCipher(clientKey, iv));
                HandshakeSuccessMessage resp = new HandshakeSuccessMessage(packet, connection);
                byte[] sessionKey = CipherBox.INSTANCE.mixKey(clientKey, resp.serverKey);
                LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, resp.serverKey);
                saveToken(resp.sessionId);
                connection.getSessionContext().changeCipher(new AesCipher(sessionKey, iv));
            } else if (command == Command.FAST_CONNECT) {
                LOGGER.info("fast connect success, message=" + packet.getStringBody());
            }
        }
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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {

    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

    }
}
