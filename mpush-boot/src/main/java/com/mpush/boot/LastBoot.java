package com.mpush.boot;

import com.mpush.common.manage.user.UserManager;
import com.mpush.tools.ConsoleLog;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class LastBoot extends BootJob {
    @Override
    public void run() {
        UserManager.INSTANCE.clearUserOnlineData();
        ConsoleLog.i("end run boot chain...");
        ConsoleLog.i("app start success...");
    }
}
