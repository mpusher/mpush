package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.Request;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.core.message.FastConnectMessage;
import com.shinemo.mpush.core.security.ReusableSession;
import com.shinemo.mpush.core.security.ReusableSessionManager;
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
    public FastConnectMessage decodeBody(byte[] body) {
        return Jsons.fromJson(body, FastConnectMessage.class);
    }

    @Override
    public void handle(FastConnectMessage body, Request request) {
        ReusableSession session = ReusableSessionManager.INSTANCE.getSession(body.tokenId);
        if (session == null) {
            request.getResponse().sendRaw("token expire".getBytes(Constants.UTF_8));
        } else if (!session.sessionInfo.deviceId.equals(body.deviceId)) {
            request.getResponse().sendRaw("error device".getBytes(Constants.UTF_8));
        } else {
            request.getConnection().setSessionInfo(session.sessionInfo);
            Map<String, Serializable> resp = new HashMap<String, Serializable>();
            resp.put("serverHost", MPushUtil.getLocalIp());
            resp.put("serverTime", System.currentTimeMillis());
            resp.put("heartbeat", Constants.HEARTBEAT_TIME);
            request.getResponse().sendRaw(Jsons.toJson(resp).getBytes(Constants.UTF_8));
        }
    }
}
