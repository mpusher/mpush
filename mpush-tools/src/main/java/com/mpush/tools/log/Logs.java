package com.mpush.tools.log;

import com.mpush.tools.config.CC;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/5/16.
 *
 * @author ohun@live.cn
 */
public interface Logs {
    boolean logInited = init();

    static boolean init() {
        if (logInited) return true;
        System.setProperty("log.home", CC.mp.log_dir);
        System.setProperty("log.root.level", CC.mp.log_level);
        LoggerFactory
                .getLogger("console")
                .info(
                        CC.mp.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true))
                );
        return true;
    }

    Logger Console = LoggerFactory.getLogger("console"),

    Conn = LoggerFactory.getLogger("mpush.conn.log"),

    Monitor = LoggerFactory.getLogger("mpush.monitor.log"),

    PUSH = LoggerFactory.getLogger("mpush.push.log"),

    HB = LoggerFactory.getLogger("mpush.heartbeat.log"),

    REDIS = LoggerFactory.getLogger("mpush.redis.log"),

    ZK = LoggerFactory.getLogger("mpush.zk.log"),

    HTTP = LoggerFactory.getLogger("mpush.http.log");
}
