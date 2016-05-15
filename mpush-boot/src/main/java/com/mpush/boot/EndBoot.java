package com.mpush.boot;

import com.mpush.common.manage.user.UserManager;
import com.mpush.monitor.service.MonitorDataCollector;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.dns.manage.DnsMappingManage;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class EndBoot extends BootJob {
    @Override
    public void run() {
        UserManager.INSTANCE.clearUserOnlineData();
        if (ConfigCenter.holder.httpProxyEnable()) {
            DnsMappingManage.holder.init();
        }
        MonitorDataCollector.start(ConfigCenter.holder.skipDump());
    }
}
