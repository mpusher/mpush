package com.mpush.boot.job;

import com.mpush.cache.redis.manager.RedisManager;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class RedisBoot extends BootJob {

    @Override
    public void run() {
        RedisManager.I.init();
        next();
    }
}
