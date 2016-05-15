package com.mpush.boot;

import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.dns.manage.DnsMappingManage;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public class HttpProxyBoot extends BootJob {
    @Override
    void run() {
        if (ConfigCenter.I.httpProxyEnable()) {
            DnsMappingManage.holder.init();
        }
        next();
    }
}
