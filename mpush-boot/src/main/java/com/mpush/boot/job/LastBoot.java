package com.mpush.boot.job;

import com.mpush.common.user.UserManager;
import com.mpush.tools.log.Logs;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class LastBoot extends BootJob {
    @Override
    public void run() {
        UserManager.INSTANCE.clearUserOnlineData();
        Logs.Console.info("end run boot chain...");
        Logs.Console.info("app start success...");
    }
}
