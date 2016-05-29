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

import com.mpush.api.push.PushContent;
import com.mpush.api.push.PushContent.PushType;
import com.mpush.api.push.PushSender;
import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

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
        for (int i = 0; i < 1000; i++) {
            PushContent content = PushContent.build(PushType.MESSAGE, "this a first push." + i);
            content.setMsgId("msgId_" + (i % 2));
            //Thread.sleep(1000);
            sender.send(Jsons.toJson(content), Arrays.asList("user-0"), new PushSender.Callback() {
                @Override
                public void onSuccess(String userId) {
                    System.err.println("push onSuccess userId=" + userId);
                }

                @Override
                public void onFailure(String userId) {
                    System.err.println("push onFailure userId=" + userId);
                }

                @Override
                public void onOffline(String userId) {
                    System.err.println("push onOffline userId=" + userId);
                }

                @Override
                public void onTimeout(String userId) {
                    System.err.println("push onTimeout userId=" + userId);
                }
            });
        }
        LockSupport.park();
    }

}