package com.mpush.tools;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/25.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];

    String JVM_LOG_PATH = "/opt/logs/mpush/";

    int THREAD_QUEUE_SIZE = 10000;
    int MIN_POOL_SIZE = 200;
    int MAX_POOL_SIZE = 500;

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
    
    int ZK_POOL_SIZE = 3;
    int ZK_THREAD_QUEUE_SIZE = 10000;

    //zk
    int ZK_MAX_RETRY = 3;
    int ZK_MIN_TIME = 5000;
    int ZK_MAX_TIME = 5000;
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 5000;
    String ZK_DEFAULT_CACHE_PATH = "/";
    String ZK_DEFAULT_DIGEST = "shinemo";

    //zk cs
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
