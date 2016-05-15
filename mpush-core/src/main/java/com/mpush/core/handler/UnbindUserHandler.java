package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.core.router.LocalRouterManager;
import com.mpush.core.router.RouterCenter;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.common.EventBus;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.BindUserMessage;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.core.router.LocalRouter;
import com.mpush.log.LogType;
import com.mpush.log.LoggerManage;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UnbindUserHandler extends BaseMessageHandler<BindUserMessage> {

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    /**
     * 目前是以用户维度来存储路由信息的，所以在删除路由信息时要判断下是否是同一个设备
     * 后续可以修改为按设备来存储路由信息。
     *
     * @param message
     */
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
            boolean unRegisterSuccess = true;
            RemoteRouterManager remoteRouterManager = RouterCenter.INSTANCE.getRemoteRouterManager();
            RemoteRouter remoteRouter = remoteRouterManager.lookup(message.userId);
            if (remoteRouter != null) {
                String deviceId = remoteRouter.getRouteValue().getDeviceId();
                if (context.deviceId.equals(deviceId)) {//判断是否是同一个设备
                    unRegisterSuccess = remoteRouterManager.unRegister(message.userId);
                }
            }

            //3.删除本地路由信息
            LocalRouterManager localRouterManager = RouterCenter.INSTANCE.getLocalRouterManager();
            LocalRouter localRouter = localRouterManager.lookup(message.userId);
            if (localRouter != null) {
                String deviceId = localRouter.getRouteValue().getSessionContext().deviceId;
                if (context.deviceId.equals(deviceId)) {//判断是否是同一个设备
                    unRegisterSuccess = localRouterManager.unRegister(message.userId) && unRegisterSuccess;
                }
            }

            //4.路由删除成功，广播用户下线事件
            if (unRegisterSuccess) {
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
