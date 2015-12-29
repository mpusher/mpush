package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.Constants;
import com.shinemo.mpush.api.MessageHandler;
import com.shinemo.mpush.api.message.ErrorMessage;
import com.shinemo.mpush.api.message.FastConnectMessage;
import com.shinemo.mpush.api.message.FastConnectSuccessMessage;
import com.shinemo.mpush.core.security.ReusableSession;
import com.shinemo.mpush.core.security.ReusableSessionManager;
import com.shinemo.mpush.tools.MPushUtil;

/**
 * Created by ohun on 2015/12/25.
 */
public final  class FastConnectHandler implements MessageHandler<FastConnectMessage> {

    @Override
    public void handle(FastConnectMessage message) {
        ReusableSession session = ReusableSessionManager.INSTANCE.getSession(message.sessionId);
        if (session == null) {
            ErrorMessage.from(message).setReason("token expire").send();
        } else if (!session.sessionContext.deviceId.equals(message.deviceId)) {
            ErrorMessage.from(message).setReason("error device").send();
        } else {
            message.getConnection().setSessionContext(session.sessionContext);

            FastConnectSuccessMessage
                    .from(message)
                    .setServerHost(MPushUtil.getLocalIp())
                    .setServerTime(System.currentTimeMillis())
                    .setHeartbeat(Constants.HEARTBEAT_TIME)
                    .send();
        }
    }
}
