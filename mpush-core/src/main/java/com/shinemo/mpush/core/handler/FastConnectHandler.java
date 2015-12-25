package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.FastConnectMessage;
import com.shinemo.mpush.core.security.ReusableToken;
import com.shinemo.mpush.core.security.ReusableTokenManager;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.MPushUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2015/12/25.
 */
public class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {
    @Override
    public FastConnectMessage decodeBody(Packet packet) {
        return Jsons.fromJson(packet.getStringBody(), FastConnectMessage.class);
    }

    @Override
    public void handle(FastConnectMessage body, Request request) {
        ReusableToken token = ReusableTokenManager.INSTANCE.getToken(body.tokenId);
        if (token == null) {
            request.getResponse().sendRaw("token expire".getBytes(Constants.UTF_8));
        } else if (!token.deviceId.equals(body.deviceId)) {
            request.getResponse().sendRaw("error device".getBytes(Constants.UTF_8));
        } else {
            SessionInfo info = new SessionInfo(token.osName, token.osVersion, token.clientVersion, token.deviceId, token.desKey);
            request.getConnection().setSessionInfo(info);
            Map<String, Serializable> resp = new HashMap<String, Serializable>();
            resp.put("serverHost", MPushUtil.getLocalIp());
            resp.put("serverTime", System.currentTimeMillis());
            resp.put("heartbeat", Constants.HEARTBEAT_TIME);
            request.getResponse().sendRaw(Jsons.toJson(resp).getBytes(Constants.UTF_8));
        }
    }
}
