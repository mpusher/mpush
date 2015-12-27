package com.shinemo.mpush.tools;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/25.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];
    
    String JVM_LOG_PATH = "/opt/";

    int THREAD_QUEUE_SIZE = 10000;
    int MIN_POOL_SIZE = 50;
    int MAX_POOL_SIZE = 500;

    int MIN_BOSS_POOL_SIZE = 10;
    int MAX_BOSS_POLL_SIZE = 50;

    int MIN_WORK_POOL_SIZE = 10;
    int MAX_WORK_POOL_SIZE = 250;
}
