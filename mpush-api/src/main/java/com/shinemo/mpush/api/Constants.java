package com.shinemo.mpush.api;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];
    int MAX_PACKET_SIZE = 1024;
    int HEADER_LEN = 13;
    short MAGIC_NUM = 1122;
    byte HB = '\n';
    int COMPRESS_LIMIT = 1024 * 10;
    byte CRYPTO_FLAG = 0x01;
    byte COMPRESS_FLAG = 0x02;
    long TIME_DELAY = 120L;

    String JVM_LOG_PATH = "/opt/";

    int THREAD_QUEUE_SIZE = 10000;
    int MIN_POOL_SIZE = 50;
    int MAX_POOL_SIZE = 500;

    int MIN_BOSS_POOL_SIZE = 10;
    int MAX_BOSS_POLL_SIZE = 50;

    int MIN_WORK_POOL_SIZE = 10;
    int MAX_WORK_POOL_SIZE = 250;
    int HEARTBEAT_TIME = 60 * 2 * 1000;//2min
}
