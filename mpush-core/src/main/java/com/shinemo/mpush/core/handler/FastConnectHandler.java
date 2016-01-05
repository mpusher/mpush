package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.FastConnectMessage;
import com.shinemo.mpush.common.message.FastConnectOkMessage;
import com.shinemo.mpush.core.session.ReusableSession;
import com.shinemo.mpush.core.session.ReusableSessionManager;
import com.shinemo.mpush.tools.MPushUtil;

/**
 * Created by ohun on 2015/12/25.
 */
public final class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {

    @Override
    public FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectMessage message) {
        ReusableSession session = ReusableSessionManager.INSTANCE.getSession(message.sessionId);
        if (session == null) {
            ErrorMessage.from(message).setReason("session expire").close();
        } else if (!session.context.deviceId.equals(message.deviceId)) {
            ErrorMessage.from(message).setReason("error device").close();
        } else {
            int heartbeat = MPushUtil.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);
            session.context.setHeartbeat(heartbeat);
            message.getConnection().setSessionContext(session.context);
            FastConnectOkMessage
                    .from(message)
                    .setServerHost(MPushUtil.getLocalIp())
                    .setServerTime(System.currentTimeMillis())
                    .setHeartbeat(heartbeat)
                    .send();
        }
    }
}
