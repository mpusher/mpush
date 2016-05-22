package com.mpush.api.spi.common;

import java.util.concurrent.Executor;

/**
 * Created by yxx on 2016/5/20.
 *
 * @author ohun@live.cn
 */
public interface ThreadPoolFactory {
    String SERVER_BOSS = "sb";
    String SERVER_WORK = "sw";
    String HTTP_CLIENT_WORK = "hcw";
    String PUSH_CLIENT_WORK = "puw";
    String EVENT_BUS = "eb";
    String MQ = "r";
    String BIZ = "b";

    Executor get(String name);
}
