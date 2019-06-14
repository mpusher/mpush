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

package com.mpush.api.push;

import com.mpush.api.MPushContext;
import com.mpush.api.service.Service;
import com.mpush.api.spi.client.PusherFactory;

import java.util.concurrent.FutureTask;

/**
 * Created by ohun on 2015/12/30.
 *
 * 推送发送器
 *
 * @author ohun@live.cn
 */
public interface PushSender extends Service {

    /**
     * 创建PushSender实例
     *
     * @return PushSender
     */
    static PushSender create() {
        return PusherFactory.create();
    }

    /**
     * 推送push消息
     *
     * @param context 推送参数
     * @return FutureTask 可用于同步调用
     */
    //FutureTask<PushResult> send(PushContext context);

    FutureTask<PushResult> sendByUserId(PushContext context);
    FutureTask<PushResult> sendByUserIds(PushContext context);
    FutureTask<PushResult> sendByAlias(PushContext context);
    FutureTask<PushResult> sendByTags(PushContext context);

//    default FutureTask<PushResult> send(String msg, String userId, PushCallback callback) {
//        return send(PushContext
//                .build(msg)
//                .setUserId(userId)
//                .setCallback(callback)
//        );
//    }
//
//    default FutureTask<PushResult> send(String msg, String userId, AckModel ackModel, PushCallback callback) {
//        return send(PushContext
//                .build(msg)
//                .setAckModel(ackModel)
//                .setUserId(userId)
//                .setCallback(callback)
//        );
//    }

    default void setMPushContext(MPushContext context) {
    }
}
