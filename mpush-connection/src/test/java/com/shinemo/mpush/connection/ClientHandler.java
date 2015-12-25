package com.shinemo.mpush.connection;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.FastConnectMessage;
import com.shinemo.mpush.core.message.HandShakeMessage;
import com.shinemo.mpush.core.security.CredentialManager;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.crypto.CryptoUtils;
import com.shinemo.mpush.tools.crypto.DESUtils;
import com.shinemo.mpush.tools.crypto.RSAUtils;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2015/12/24.
 */
public class ClientHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private String clientKey = RandomStringUtils.randomAscii(CryptoUtils.DES_KEY_SIZE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String token = getToken();
        if (Strings.isBlank(token)) {
            RSAPublicKey publicKey = CredentialManager.INSTANCE.getPublicKey();
            HandShakeMessage message = new HandShakeMessage();
            message.clientKey = clientKey;
            message.clientVersion = "1.0.1";
            message.deviceId = "1111111111111";
            message.osName = "android";
            message.osVersion = "5.0";
            message.timestamp = System.currentTimeMillis();

            Packet packet = new Packet();
            packet.command = Command.Handshake.cmd;
            packet.version = 0;
            packet.flags = 0;
            packet.msgId = 1;
            packet.body = RSAUtils.encryptByPublicKey(Jsons.toJson(message).getBytes(Constants.UTF_8), publicKey);
            ctx.writeAndFlush(packet);
        } else {
            FastConnectMessage message = new FastConnectMessage();
            message.deviceId = "1111111111111";
            message.tokenId = token;
            Packet packet = new Packet();
            packet.command = Command.FastConnect.cmd;
            packet.version = 0;
            packet.flags = 0;
            packet.msgId = 1;
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
            Command command = Command.toCMD(packet.command);
            if (command == Command.Handshake) {
                String raw = new String(DESUtils.decryptDES(packet.body, clientKey), Constants.UTF_8);
                Map<String, Serializable> resp = Jsons.fromJson(raw, Map.class);
                LOGGER.info("hand shake success, message=" + raw);
                String desKey = CryptoUtils.mixString(clientKey, (String) resp.get("serverKey"));
                LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", desKey, clientKey, resp.get("serverKey"));
                saveToken((String) resp.get("tokenId"));
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
