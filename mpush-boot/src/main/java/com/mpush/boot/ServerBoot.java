package com.mpush.boot;

import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKServerNode;
import com.mpush.api.Server;
import com.mpush.tools.Jsons;
import com.mpush.tools.thread.threadpool.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ServerBoot extends BootJob {
    private final Logger log = LoggerFactory.getLogger(ServerBoot.class);

    private final Server server;
    private final ZKServerNode node;

    public ServerBoot(Server server, ZKServerNode node) {
        this.server = server;
        this.node = node;
    }

    @Override
    public void run() {
        ThreadPoolManager.newThread(server.getClass().getSimpleName(), new Runnable() {
            @Override
            public void run() {
                server.init();
                server.start(new Server.Listener() {
                    @Override
                    public void onSuccess() {
                        log.error("mpush app start " + server.getClass().getSimpleName() + " server success....");
                        if (node != null) {
                            registerServerToZk(node.getZkPath(), Jsons.toJson(node));
                        }
                        next();
                    }

                    @Override
                    public void onFailure(String message) {
                        log.error("mpush app start " + server.getClass().getSimpleName()
                                + " server failure, jvm exit with code -1");
                        System.exit(-1);
                    }
                });
            }
        }).start();
    }

    //step7  注册应用到zk
    public void registerServerToZk(String path, String value) {
        ZKClient.I.registerEphemeralSequential(path, value);
        log.error("register server to zk:{},{}", path, value);
    }
}
