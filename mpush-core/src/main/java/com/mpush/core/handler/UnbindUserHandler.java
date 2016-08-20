/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.api.connection.Connection;
import com.mpush.api.connection.SessionContext;
import com.mpush.api.event.UserOfflineEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.BindUserMessage;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.core.router.LocalRouter;
import com.mpush.core.router.LocalRouterManager;
import com.mpush.core.router.RouterCenter;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;


/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
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
            Logs.Conn.info("unbind user failure invalid param, session={}", message.getConnection().getSessionContext());
            return;
        }

        //1.解绑用户时先看下是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            //2.先删除远程路由, 必须是同一个设备才允许解绑
            boolean unRegisterSuccess = true;
            int clientType = context.getClientType();
            String userId = message.userId;
            RemoteRouterManager remoteRouterManager = RouterCenter.I.getRemoteRouterManager();
            RemoteRouter remoteRouter = remoteRouterManager.lookup(userId, clientType);
            if (remoteRouter != null) {
                String deviceId = remoteRouter.getRouteValue().getDeviceId();
                if (context.deviceId.equals(deviceId)) {//判断是否是同一个设备
                    unRegisterSuccess = remoteRouterManager.unRegister(userId, clientType);
                }
            }

            //3.删除本地路由信息
            LocalRouterManager localRouterManager = RouterCenter.I.getLocalRouterManager();
            LocalRouter localRouter = localRouterManager.lookup(userId, clientType);
            if (localRouter != null) {
                String deviceId = localRouter.getRouteValue().getSessionContext().deviceId;
                if (context.deviceId.equals(deviceId)) {//判断是否是同一个设备
                    unRegisterSuccess = localRouterManager.unRegister(userId, clientType) && unRegisterSuccess;
                }
            }

            //4.路由删除成功，广播用户下线事件
            if (unRegisterSuccess) {
                context.userId = null;
                EventBus.I.post(new UserOfflineEvent(message.getConnection(), userId));
                OkMessage.from(message).setData("unbind success").send();
                Logs.Conn.info("unbind user success, userId={}, session={}", userId, context);
            } else {
                ErrorMessage.from(message).setReason("unbind failed").send();
                Logs.Conn.info("unbind user failure, register router failure, userId={}, session={}", userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            Logs.Conn.info("unbind user failure not handshake, userId={}, session={}", message.userId, context);
        }
    }
}
