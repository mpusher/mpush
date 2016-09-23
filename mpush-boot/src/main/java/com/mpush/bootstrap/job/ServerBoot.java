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

import com.mpush.api.service.Listener;
import com.mpush.api.service.Server;
import com.mpush.tools.Jsons;
import com.mpush.tools.log.Logs;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKRegister;
import com.mpush.zk.node.ZKServerNode;

import java.util.concurrent.TimeUnit;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public final class ServerBoot extends BootJob {
    private final Server server;
    private final ZKServerNode node;

    public ServerBoot(Server server, ZKServerNode node) {
        this.server = server;
        this.node = node;
    }

    @Override
    public void start() {
        String serverName = server.getClass().getSimpleName();
        ThreadPoolManager.I.newThread(serverName, () -> {
            server.init();
            server.start(new Listener() {
                @Override
                public void onSuccess(Object... args) {
                    Logs.Console.info("start {} success listen:{}", serverName, args[0]);
                    if (node != null) {//注册应用到zk
                        ZKRegister.build().setEphemeral(true).setNode(node).register();
                        Logs.Console.info("register server node={} to zk path={}", node, node.getNodePath());
                    }
                    startNext();
                }

                @Override
                public void onFailure(Throwable cause) {
                    Logs.Console.error("start " + serverName + " failure, jvm exit with code -1", cause);
                    System.exit(-1);
                }
            });
        }).start();
    }

    @Override
    protected void stop() {
        try {
            server.stop().get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            Logs.Console.error("stop server error:", e);
        }
        stopNext();
    }
}
