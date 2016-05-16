package com.mpush.core.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.FastConnectMessage;
import com.mpush.common.message.FastConnectOkMessage;
import com.mpush.core.session.ReusableSession;
import com.mpush.core.session.ReusableSessionManager;
import com.mpush.log.Logs;
import com.mpush.tools.MPushUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public final class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {
    public static final Logger LOGGER = LoggerFactory.getLogger(FastConnectHandler.class);

    @Override
    public FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectMessage message) {
        //从缓存中心查询session
        ReusableSession session = ReusableSessionManager.INSTANCE.querySession(message.sessionId);

        if (session == null) {
            //1.没查到说明session已经失效了
            ErrorMessage.from(message).setReason("session expired").send();
            Logs.Conn.info("fast connect failure, session is expired, sessionId={}, deviceId={}", message.sessionId, message.deviceId);
        } else if (!session.context.deviceId.equals(message.deviceId)) {
            //2.非法的设备, 当前设备不是上次生成session时的设备
            ErrorMessage.from(message).setReason("invalid device").send();
            Logs.Conn.info("fast connect failure, not the same device, deviceId={}, session={}", message.deviceId, session.context);
        } else {
            //3.校验成功，重新计算心跳，完成快速重连
            int heartbeat = MPushUtil.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

            session.context.setHeartbeat(heartbeat);
            message.getConnection().setSessionContext(session.context);

            FastConnectOkMessage
                    .from(message)
                    .setHeartbeat(heartbeat)
                    .send();
            Logs.Conn.info("fast connect success, session={}", session.context);
        }
    }
}
