package com.mpush.boot.job;

import com.mpush.common.net.DnsMappingManager;
import com.mpush.tools.config.CC;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public class HttpProxyBoot extends BootJob {
    @Override
    void run() {
        if (CC.mp.http.proxy_enable) {
            DnsMappingManager.I.init();
            DnsMappingManager.I.start();
        }
        next();
    }
}
