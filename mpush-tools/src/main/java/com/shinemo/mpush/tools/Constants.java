package com.shinemo.mpush.tools;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/25.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    int HEARTBEAT_TIME = 1000 * 60 * 1;//5min
    byte[] EMPTY_BYTES = new byte[0];

    String JVM_LOG_PATH = "/opt/";

    int THREAD_QUEUE_SIZE = 10000;
    int MIN_POOL_SIZE = 50;
    int MAX_POOL_SIZE = 500;

    int MIN_BOSS_POOL_SIZE = 10;
    int MAX_BOSS_POLL_SIZE = 50;

    int MIN_WORK_POOL_SIZE = 10;
    int MAX_WORK_POOL_SIZE = 250;

    //zk
    int ZK_MAX_RETRY = 3;
    int ZK_MIN_TIME = 5000;
    int ZK_MAX_TIME = 5000;
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 5000;
    String ZK_DEFAULT_CACHE_PATH = "/";
    String ZK_DEFAULT_DIGEST = "shinemo";
    String ZK_IPS = "127.0.0.1:2181";

    //zk cs
    String ZK_NAME_SPACE = "mpush";
    //所有机器启动的时候注册ip的地方
    String ZK_REGISTER_HOST = "/allhost";
    String ZK_REGISTER_PREFIX_NAME = "machine";
    String ZK_KICK = "kickoff";
    
    //redis
    int REDIS_TIMEOUT = 2000;
    int REDIS_MAX_TOTAL = 8;
    int REDIS_MAX_IDLE = 4;
    int REDIS_MIN_IDLE = 1;
    int REDIS_MAX_WAITMILLIS = 5000;
    int REDIS_MIN_EVICTABLEIDLETIMEMILLIS = 300000;
    int REDIS_NUMTESTSPEREVICTIONRUN = 3;
    int REDIS_TIMEBETWEENEVICTIONRUNMILLIS = 60000;
    boolean REDIS_TESTONBORROW = false;
    boolean REDIS_TESTONRETURN = false;
    boolean REDIS_TESTWHILEIDLE = false;
}
