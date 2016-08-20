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
import com.mpush.api.event.UserOnlineEvent;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.BindUserMessage;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.core.router.RouterCenter;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class BindUserHandler extends BaseMessageHandler<BindUserMessage> {

    @Override
    public BindUserMessage decode(Packet packet, Connection connection) {
        return new BindUserMessage(packet, connection);
    }

    @Override
    public void handle(BindUserMessage message) {
        if (Strings.isNullOrEmpty(message.userId)) {
            ErrorMessage.from(message).setReason("invalid param").close();
            Logs.Conn.info("bind user failure for invalid param, session={}", message.getConnection().getSessionContext());
            return;
        }
        //1.绑定用户时先看下是否握手成功
        SessionContext context = message.getConnection().getSessionContext();
        if (context.handshakeOk()) {
            //2.如果握手成功，就把用户链接信息注册到路由中心，本地和远程各一份
            boolean success = RouterCenter.I.register(message.userId, message.getConnection());
            if (success) {
                context.userId = message.userId;
                EventBus.I.post(new UserOnlineEvent(message.getConnection(), message.userId));
                OkMessage.from(message).setData("bind success").send();
                Logs.Conn.info("bind user success, userId={}, session={}", message.userId, context);
            } else {
                //3.注册失败再处理下，防止本地注册成功，远程注册失败的情况，只有都成功了才叫成功
                RouterCenter.I.unRegister(message.userId, context.getClientType());
                ErrorMessage.from(message).setReason("bind failed").close();
                Logs.Conn.info("bind user failure, userId={}, session={}", message.userId, context);
            }
        } else {
            ErrorMessage.from(message).setReason("not handshake").close();
            Logs.Conn.info("bind user failure for not handshake, userId={}, session={}", message.userId, context);
        }
    }
}
