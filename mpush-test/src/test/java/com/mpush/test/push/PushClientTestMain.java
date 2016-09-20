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

package com.mpush.test.push;

import com.mpush.api.push.*;
import com.mpush.api.router.ClientLocation;
import com.mpush.tools.log.Logs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * Created by ohun on 2016/1/7.
 *
 * @author ohun@live.cn
 */
public class PushClientTestMain {
    public static void main(String[] args) throws Exception {
        Logs.init();
        PushSender sender = PushSender.create();
        sender.start().get();
        PushMsg msg = PushMsg.build(MsgType.MESSAGE, "this a first push.");
        msg.setMsgId("msgId_0");

        PushContext context = PushContext.build(msg)
                .setBroadcast(false)
                .setUserIds(Arrays.asList("user-0", "user-1"))
                .setTimeout(100000)
                .setCallback(new PushCallback() {
                    @Override
                    public void onResult(PushResult result) {
                        System.err.println(result);
                    }
                });
        Thread.sleep(1000);
        FutureTask<Boolean> future = sender.send(context);
        future.get();
    }

}