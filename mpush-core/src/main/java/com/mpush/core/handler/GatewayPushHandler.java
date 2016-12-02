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

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.core.push.*;
import com.mpush.tools.config.CC;

import static com.mpush.tools.config.CC.mp.push.flow_control.*;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {

    private final PushCenter pushCenter = PushCenter.I;

    private final GlobalFlowControl globalFlowControl = new GlobalFlowControl(
            global.limit, global.max, global.duration
    );

    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    /**
     * 处理PushClient发送过来的Push推送请求
     * <p>
     * 查寻路由策略，先查本地路由，本地不存在，查远程，（注意：有可能远程查到也是本机IP）
     * <p>
     * 正常情况本地路由应该存在，如果不存在或链接失效，有以下几种情况：
     * <p>
     * 1.客户端重连，并且链接到了其他机器
     * 2.客户端下线，本地路由失效，远程路由还未清除
     * 3.PushClient使用了本地缓存，但缓存数据已经和实际情况不一致了
     * <p>
     * 对于三种情况的处理方式是, 再重新查寻下远程路由：
     * 1.如果发现远程路由是本机，直接删除，因为此时的路由已失效 (解决场景2)
     * 2.如果用户真在另一台机器，让PushClient清理下本地缓存后，重新推送 (解决场景1,3)
     * <p>
     *
     * @param message message
     */
    @Override
    public void handle(GatewayPushMessage message) {
        if (message.isBroadcast()) {
            FlowControl flowControl = (message.taskId == null)
                    ? new FastFlowControl(broadcast.limit, broadcast.max, broadcast.duration)
                    : new RedisFlowControl(message.taskId, broadcast.max);
            pushCenter.addTask(new BroadcastPushTask(message, flowControl));
        } else {
            pushCenter.addTask(new SingleUserPushTask(message, globalFlowControl));
        }
    }
}
