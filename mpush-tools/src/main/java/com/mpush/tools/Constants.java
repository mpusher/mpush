package com.mpush.tools;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];

    int MIN_BOSS_POOL_SIZE = 50;
    int MAX_BOSS_POOL_SIZE = 100;
    int BOSS_THREAD_QUEUE_SIZE = 10000;

    int MIN_WORK_POOL_SIZE = 50;
    int MAX_WORK_POOL_SIZE = 150;
    int WORK_THREAD_QUEUE_SIZE = 10000;

    int BIZ_POOL_SIZE = 20;

    int EVENT_BUS_POOL_SIZE = 10;

    int REDIS_POOL_SIZE = 3;
    int REDIS_THREAD_QUEUE_SIZE = 10000;

    //redis
    int REDIS_TIMEOUT = 2000;
    int REDIS_MAX_TOTAL = 8;
    int REDIS_MAX_IDLE = 4;
    int REDIS_MIN_IDLE = 1;
    int REDIS_MAX_WAIT_MILLIS = 5000;
    int REDIS_MIN_EVICTABLE_IDLE_TIME_MILLIS = 300000;
    int REDIS_NUM_TESTS_PER_EVICTION_RUN = 3;
    int REDIS_TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000;
    boolean REDIS_TEST_ON_BORROW = false;
    boolean REDIS_TEST_ON_RETURN = false;
    boolean REDIS_TEST_WHILE_IDLE = false;
}
