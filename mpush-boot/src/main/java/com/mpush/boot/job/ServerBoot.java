package com.mpush.boot.job;

import com.mpush.api.Server;
import com.mpush.tools.log.Logs;
import com.mpush.tools.Jsons;
import com.mpush.tools.thread.pool.ThreadPoolManager;
import com.mpush.zk.ZKClient;
import com.mpush.zk.node.ZKServerNode;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ServerBoot extends BootJob {
    private final Server server;
    private final ZKServerNode node;

    public ServerBoot(Server server, ZKServerNode node) {
        this.server = server;
        this.node = node;
    }

    @Override
    public void run() {
        final String serverName = server.getClass().getSimpleName();
        ThreadPoolManager.I.newThread(serverName, new Runnable() {
            @Override
            public void run() {
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess(Object... args) {
                        Logs.Console.info("start " + serverName + " success listen:" + args[0]);
                        if (node != null) {
                            registerServerToZk(node.getZkPath(), Jsons.toJson(node));
                        }
                        next();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        Logs.Console.error("start " + serverName + " failure, jvm exit with code -1", cause);
                        System.exit(-1);
                    }
                });
            }
        }).start();
    }

    //step7  注册应用到zk
    public void registerServerToZk(String path, String value) {
        ZKClient.I.registerEphemeralSequential(path, value);
        Logs.Console.info("register server node=" + value + " to zk name=" + path);
    }
}
