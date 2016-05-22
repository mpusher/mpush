package com.mpush.boot.job;

import com.mpush.api.exception.BootException;
import com.mpush.zk.ZKClient;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ZKBoot extends BootJob {

    @Override
    public void run() {
        if (ZKClient.I.getZKConfig() != null) {
            next();
        } else {
            throw new BootException("init zk client failure");
        }
    }
}
