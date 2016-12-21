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

import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        Executor executor = Executors.newFixedThreadPool(10);
        Statistics statistics = new Statistics();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(
                () -> {
                    statistics.limitCount.set(1000);
                    System.out.println("====================" + LocalTime.now() + ":" + statistics.qps() + ":" + statistics.sendCount.get());
                }
                , 1, 1, TimeUnit.SECONDS
        );
        for (int k = 0; k < 100; k++) {
            for (int i = 0; i < 1000; i++) {
                executor.execute(new PushTask(sender, i, statistics));
            }
        }


        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30000));
    }

    private static class PushTask implements Runnable {
        PushSender sender;
        private int i;
        Statistics statistics;

        public PushTask(PushSender sender, int i, Statistics statistics) {
            this.sender = sender;
            this.i = i;
            this.statistics = statistics;
        }

        @Override
        public void run() {
            while (statistics.limitCount.get() <= 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {

                }
            }
            PushMsg msg = PushMsg.build(MsgType.MESSAGE, "this a first push.");
            msg.setMsgId("msgId_" + i);

            PushContext context = PushContext.build(msg)
                    .setAckModel(AckModel.NO_ACK)
                    .setUserId("user-" + i)
                    .setBroadcast(false)
                    //.setTags(Sets.newHashSet("test"))
                    //.setCondition("tags&&tags.indexOf('test')!=-1")
                    //.setUserIds(Arrays.asList("user-0", "user-1"))
                    .setTimeout(60000)
                    .setCallback(new PushCallback() {
                        @Override
                        public void onResult(PushResult result) {
                           //System.err.println("\n\n" + result);
                        }
                    });
            FutureTask<PushResult> future = sender.send(context);
            statistics.sendCount.incrementAndGet();
            statistics.limitCount.decrementAndGet();
        }
    }

    private static class Statistics {
        AtomicInteger sendCount = new AtomicInteger();
        AtomicInteger limitCount = new AtomicInteger(3000);
        long start = System.nanoTime();

        public long qps() {
            return sendCount.get() / (TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
        }
    }
}