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
import com.mpush.common.qps.FlowControl;
import com.mpush.common.qps.GlobalFlowControl;
import com.mpush.tools.log.Logs;
import org.junit.Test;

import java.time.LocalTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by ohun on 2016/1/7.
 *
 * @author ohun@live.cn
 */
public class PushClientTestMain2 {

    public static void main(String[] args) throws Exception {
        new PushClientTestMain2().testPush();
    }

    @Test
    public void testPush() throws Exception {
        Logs.init();
        PushSender sender = PushSender.create();
        sender.start().join();
        Thread.sleep(1000);


        Statistics statistics = new Statistics();
        FlowControl flowControl = new GlobalFlowControl(1000);// qps=1000

        ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(4);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("time=" + LocalTime.now()
                    + ", flowControl=" + flowControl.report()
                    + ", statistics=" + statistics
            );
        }, 1, 1, TimeUnit.SECONDS);

        for (int k = 0; k < 1000; k++) {
            for (int i = 0; i < 1; i++) {

                while (service.getQueue().size() > 1000) Thread.sleep(1); // 防止内存溢出

                PushMsg msg = PushMsg.build(MsgType.MESSAGE, "this a first push.");
                msg.setMsgId("msgId_" + i);

                PushContext context = PushContext.build(msg)
                        .setAckModel(AckModel.NO_ACK)
                        .setUserId("user-" + i)
                        .setBroadcast(false)
                        .setTimeout(60000)
                        .setCallback(new PushCallback() {
                            @Override
                            public void onResult(PushResult result) {
                                statistics.add(result.resultCode);
                            }
                        });
                service.execute(new PushTask(sender, context, service, flowControl, statistics));
            }
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30000));
    }

    private static class PushTask implements Runnable {
        PushSender sender;
        FlowControl flowControl;
        Statistics statistics;
        ScheduledExecutorService executor;
        PushContext context;

        public PushTask(PushSender sender,
                        PushContext context,
                        ScheduledExecutorService executor,
                        FlowControl flowControl,
                        Statistics statistics) {
            this.sender = sender;
            this.context = context;
            this.flowControl = flowControl;
            this.executor = executor;
            this.statistics = statistics;
        }

        @Override
        public void run() {
            if (flowControl.checkQps()) {
                FutureTask<PushResult> future = sender.send(context);
            } else {
                executor.schedule(this, flowControl.getDelay(), TimeUnit.NANOSECONDS);
            }
        }
    }

    private static class Statistics {
        final AtomicInteger successNum = new AtomicInteger();
        final AtomicInteger failureNum = new AtomicInteger();
        final AtomicInteger offlineNum = new AtomicInteger();
        final AtomicInteger timeoutNum = new AtomicInteger();
        AtomicInteger[] counters = new AtomicInteger[]{successNum, failureNum, offlineNum, timeoutNum};

        private void add(int code) {
            counters[code - 1].incrementAndGet();
        }

        @Override
        public String toString() {
            return "{" +
                    "successNum=" + successNum +
                    ", offlineNum=" + offlineNum +
                    ", timeoutNum=" + timeoutNum +
                    ", failureNum=" + failureNum +
                    '}';
        }
    }
}