package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.HandShakeMessage;
import com.shinemo.mpush.core.security.CredentialManager;
import com.shinemo.mpush.core.security.ReusableToken;
import com.shinemo.mpush.core.security.ReusableTokenManager;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;
import com.shinemo.mpush.tools.crypto.CryptoUtils;
import com.shinemo.mpush.tools.crypto.DESUtils;
import com.shinemo.mpush.tools.crypto.RSAUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandShakeHandler extends BaseMessageHandler<HandShakeMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(HandShakeHandler.class);

    @Override
    public HandShakeMessage decodeBody(Packet packet) {
        RSAPrivateKey privateKey = CredentialManager.INSTANCE.getPrivateKey();
        byte[] rawData = RSAUtils.decryptByPrivateKey(packet.body, privateKey);
        return Jsons.fromJson(new String(rawData, Constants.UTF_8), HandShakeMessage.class);
    }

    @Override
    public void handle(HandShakeMessage body, Request request) {
        String serverKey = RandomStringUtils.randomAscii(CryptoUtils.DES_KEY_SIZE);
        String clientKey = body.clientKey;
        String desKey = CryptoUtils.mixString(clientKey, serverKey);//生成混淆密钥
        SessionInfo info = new SessionInfo(body.osName, body.osVersion, body.clientVersion, body.deviceId, desKey);
        request.getConnection().setSessionInfo(info);
        ReusableToken token = ReusableTokenManager.INSTANCE.genToken(info);
        ReusableTokenManager.INSTANCE.saveToken(token);
        Map<String, Serializable> resp = new HashMap<String, Serializable>();
        resp.put("serverKey", serverKey);
        resp.put("serverHost", MPushUtil.getLocalIp());
        resp.put("serverTime", System.currentTimeMillis());
        resp.put("heartbeat", Constants.HEARTBEAT_TIME);
        resp.put("tokenId", token.tokenId);
        resp.put("tokenExpire", token.expireTime);
        byte[] responseData = DESUtils.encryptDES(Jsons.toJson(resp).getBytes(Constants.UTF_8), clientKey);
        request.getResponse().sendRaw(responseData);
        LOGGER.info("会话密钥：{}，clientKey={}, serverKey={}", desKey, clientKey, serverKey);

    }
}
