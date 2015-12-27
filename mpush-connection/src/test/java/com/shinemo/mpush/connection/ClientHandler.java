package com.shinemo.mpush.connection;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.FastConnectMessage;
import com.shinemo.mpush.core.message.HandshakeMessage;
import com.shinemo.mpush.core.message.HandshakeSuccessMsg;
import com.shinemo.mpush.core.security.CipherManager;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.crypto.CryptoUtils;
import com.shinemo.mpush.tools.crypto.AESUtils;
import com.shinemo.mpush.tools.crypto.RSAUtils;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private byte[] clientKey = CipherManager.INSTANCE.randomAESKey();
    private byte[] iv = CipherManager.INSTANCE.randomAESIV();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String token = getToken();
        if (!Strings.isBlank(token)) {
            RSAPublicKey publicKey = CipherManager.INSTANCE.getPublicKey();
            HandshakeMessage message = new HandshakeMessage();
            message.clientKey = clientKey;
            message.iv = iv;
            message.clientVersion = "1.0.1";
            message.deviceId = "1111111111111";
            message.osName = "android";
            message.osVersion = "5.0";
            message.timestamp = System.currentTimeMillis();

            Packet packet = new Packet();
            packet.cmd = Command.Handshake.cmd;
            packet.sessionId = 1;
            packet.body = RSAUtils.encryptByPublicKey(Jsons.toJson(message).getBytes(Constants.UTF_8), publicKey);
            ctx.writeAndFlush(packet);
        } else {
            FastConnectMessage message = new FastConnectMessage();
            message.deviceId = "1111111111111";
            message.tokenId = token;
            Packet packet = new Packet();
            packet.cmd = Command.FastConnect.cmd;
            packet.sessionId = 1;
            packet.body = Jsons.toJson(message).getBytes(Constants.UTF_8);
            ctx.writeAndFlush(packet);
        }
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelInactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        LOGGER.info("client," + ctx.channel().remoteAddress().toString(), "channelRead", msg);
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            Command command = Command.toCMD(packet.cmd);
            if (command == Command.Handshake) {
                String raw = new String(AESUtils.decrypt(packet.body, clientKey, iv), Constants.UTF_8);
                HandshakeSuccessMsg resp = Jsons.fromJson(raw, HandshakeSuccessMsg.class);
                LOGGER.info("hand shake success, message=" + raw);
                byte[] sessionKey = CipherManager.INSTANCE.mixKey(clientKey, resp.serverKey);
                LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", sessionKey, clientKey, resp.serverKey);
                saveToken(resp.sessionId);
            } else if (command == Command.FastConnect) {
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
}
