package com.shinemo.mpush.core.handler;

import com.google.common.base.Strings;
import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.api.event.UserOfflineEvent;
import com.shinemo.mpush.api.event.UserOnlineEvent;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.common.EventBus;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.message.BindUserMessage;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.router.RemoteRouter;
import com.shinemo.mpush.common.router.RemoteRouterManager;
import com.shinemo.mpush.core.router.RouterCenter;
import com.shinemo.mpush.log.LogType;
import com.shinemo.mpush.log.LoggerManage;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UnbindUserHandler extends BaseMessageHandler<BindUserMessage> {

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    @Override
    public void handle(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            LoggerManage.info(LogType.CONNECTION, "unbind user failure invalid param, session={}", message.getConnection().getSessionContext());
            return;
        }
        //1.解绑用户时先看下是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            //2.先删除远程路由, 必须是同一个设备才允许解绑
            RemoteRouterManager remoteRouterManager = RouterCenter.INSTANCE.getRemoteRouterManager();
            RemoteRouter remoteRouter = remoteRouterManager.lookup(message.userId);
            if (remoteRouter != null) {
                String deviceId = remoteRouter.getRouteValue().getDeviceId();
                if (context.deviceId.equals(deviceId)) {//判断是否是同一个设备
                    remoteRouterManager.unRegister(message.userId);
                }
            }
            //3.删除本地路由信息
            boolean success = RouterCenter.INSTANCE.getLocalRouterManager().unRegister(message.userId);
            if (success) {

                EventBus.INSTANCE.post(new UserOfflineEvent(message.getConnection(), message.userId));

                OkMessage.from(message).setData("unbind success").send();
                LoggerManage.info(LogType.CONNECTION, "unbind user success, userId={}, session={}", message.userId, context);
            } else {
                ErrorMessage.from(message).setReason("unbind failed").send();
                LoggerManage.info(LogType.CONNECTION, "unbind user failure, register router failure, userId={}, session={}", message.userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            LoggerManage.info(LogType.CONNECTION, "unbind user failure not handshake, userId={}, session={}", message.userId, context);
        }
    }
}
