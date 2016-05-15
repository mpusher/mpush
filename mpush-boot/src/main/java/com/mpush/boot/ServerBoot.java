package com.mpush.boot;

import com.mpush.api.Server;
import com.mpush.tools.ConsoleLog;
import com.mpush.tools.Jsons;
import com.mpush.tools.thread.threadpool.ThreadPoolManager;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKServerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ServerBoot extends BootJob {
    private final Logger logger = LoggerFactory.getLogger(ServerBoot.class);

    private final Server server;
    private final ZKServerNode node;

    public ServerBoot(Server server, ZKServerNode node) {
        this.server = server;
        this.node = node;
    }

    @Override
    public void run() {
        final String serverName = server.getClass().getSimpleName();
        ThreadPoolManager.newThread(serverName, new Runnable() {
            @Override
            public void run() {
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess(int port) {
                        String msg = "start " + serverName + " success listen:" + port;
                        logger.error(msg);
                        ConsoleLog.i(msg);
                        if (node != null) {
                            registerServerToZk(node.getZkPath(), Jsons.toJson(node));
                        }
                        next();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        String msg = "start " + serverName + " failure, jvm exit with code -1";
                        logger.error(msg);
                        ConsoleLog.e(cause, msg);
                        System.exit(-1);
                    }
                });
            }
        }).start();
    }

    //step7  注册应用到zk
    public void registerServerToZk(String path, String value) {
        ZKClient.I.registerEphemeralSequential(path, value);
        String msg = "register server node=" + value + " to zk path=" + path;
        logger.error(msg);
        ConsoleLog.i(msg);
    }
}
