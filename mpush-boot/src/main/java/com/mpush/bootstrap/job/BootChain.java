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

package com.mpush.bootstrap.job;

import com.mpush.api.event.ServerShutdownEvent;
import com.mpush.api.event.ServerStartupEvent;
import com.mpush.api.spi.core.ServerEventListenerFactory;
import com.mpush.tools.event.EventBus;
import com.mpush.tools.log.Logs;

import java.util.function.Supplier;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public final class BootChain {
    private final BootJob boot = new BootJob() {
        {
            ServerEventListenerFactory.create();// 初始化服务监听
        }

        @Override
        protected void start() {
            Logs.Console.info("bootstrap chain starting...");
            startNext();
        }

        @Override
        protected void stop() {
            stopNext();
            Logs.Console.info("bootstrap chain stopped.");
            Logs.Console.info("===================================================================");
            Logs.Console.info("====================MPUSH SERVER STOPPED SUCCESS===================");
            Logs.Console.info("===================================================================");
        }
    };

    private BootJob last = boot;

    public void start() {
        boot.start();
    }

    public void stop() {
        boot.stop();
    }

    public static BootChain chain() {
        return new BootChain();
    }

    public BootChain boot() {
        return this;
    }

    public void end() {
        setNext(new BootJob() {
            @Override
            protected void start() {
                EventBus.post(new ServerStartupEvent());
                Logs.Console.info("bootstrap chain started.");
                Logs.Console.info("===================================================================");
                Logs.Console.info("====================MPUSH SERVER START SUCCESS=====================");
                Logs.Console.info("===================================================================");
            }

            @Override
            protected void stop() {
                Logs.Console.info("bootstrap chain stopping...");
                EventBus.post(new ServerShutdownEvent());
            }

            @Override
            protected String getName() {
                return "LastBoot";
            }
        });
    }

    public BootChain setNext(BootJob bootJob) {
        this.last = last.setNext(bootJob);
        return this;
    }

    public BootChain setNext(Supplier<BootJob> next, boolean enabled) {
        if (enabled) {
            return setNext(next.get());
        }
        return this;
    }
}
