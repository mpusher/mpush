package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.core.message.FastConnectMessage;
import com.shinemo.mpush.core.security.ReusableSession;
import com.shinemo.mpush.core.security.ReusableSessionManager;

/**
 * Created by ohun on 2015/12/25.
 */
public class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {

    @Override
    public void handle(FastConnectMessage message) {
        ReusableSession session = ReusableSessionManager.INSTANCE.getSession(message.tokenId);
        if (session == null) {
            //message.sendRaw("token expire".getBytes(Constants.UTF_8));
        } else if (!session.sessionContext.deviceId.equals(message.deviceId)) {
            //message.sendRaw("error device".getBytes(Constants.UTF_8));
        } else {
            /*request.getConnection().setSessionInfo(session.sessionContext);
            Map<String, Serializable> resp = new HashMap<String, Serializable>();
            resp.put("serverHost", MPushUtil.getLocalIp());
            resp.put("serverTime", System.currentTimeMillis());
            resp.put("heartbeat", Constants.HEARTBEAT_TIME);
            request.getResponse().sendRaw(Jsons.toJson(resp).getBytes(Constants.UTF_8));*/
        }
    }
}
