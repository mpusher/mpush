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

import com.google.common.collect.Sets;
import com.mpush.api.push.*;
import com.mpush.tools.log.Logs;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2016/1/7.
 *
 * @author ohun@live.cn
 */
public class PushClientTestMain {

    public static void main(String[] args) throws Exception {
        new PushClientTestMain().testPush();
    }

    @Test
    public void testPush() throws Exception {
        Logs.init();
        PushSender sender = PushSender.create();
        sender.start().join();
        Thread.sleep(1000);

        for (int i = 0; i < 1; i++) {

            PushMsg msg = PushMsg.build(MsgType.MESSAGE, "this a first push.");
            msg.setMsgId("msgId_" + i);

            PushContext context = PushContext.build(msg)
                    .setAckModel(AckModel.AUTO_ACK)
                    .setUserId("user-" + i)
                    .setBroadcast(false)
                    //.setTags(Sets.newHashSet("test"))
                    //.setCondition("tags&&tags.indexOf('test')!=-1")
                    //.setUserIds(Arrays.asList("user-0", "user-1"))
                    .setTimeout(2000)
                    .setCallback(new PushCallback() {
                        @Override
                        public void onResult(PushResult result) {
                            System.err.println("\n\n" + result);
                        }
                    });
            FutureTask<PushResult> future = sender.send(context);

            //System.err.println("\n\n" + future.get());
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30));
    }

}