package com.shinemo.mpush.core.security;

import com.shinemo.mpush.api.SessionInfo;
import com.shinemo.mpush.tools.crypto.MD5Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2015/12/25.
 */
public class ReusableSessionManager {
    public static final ReusableSessionManager INSTANCE = new ReusableSessionManager();
    private static final int EXPIRE_TIME = 24 * 60 * 60 * 1000;
    private final Map<String, ReusableSession> tokenCache = new ConcurrentHashMap<String, ReusableSession>();

    public boolean saveSession(ReusableSession session) {
        tokenCache.put(session.sessionId, session);
        return true;
    }

    public ReusableSession getSession(String sessionId) {
        return tokenCache.get(sessionId);
    }

    public ReusableSession genSession(SessionInfo info) {
        /**
         * 先生成key，需要保证半个周期内同一个设备生成的key是相同的
         */
        long partition = System.currentTimeMillis() / (EXPIRE_TIME / 2);//把当前时间按照半个周期划分出一个当前所属于的区域
        StringBuilder sb = new StringBuilder();
        sb.append(info.deviceId).append(partition);
        ReusableSession v = new ReusableSession();
        v.sessionInfo = info;
        v.sessionId = MD5Utils.encrypt(sb.toString());
        /**
         * 计算失效时间
         */
        long nowTime = System.currentTimeMillis();
        long willExpire = (nowTime / EXPIRE_TIME + 1) * EXPIRE_TIME;//预计的到下个周期的失效时间

        //有可能到绝对周期的时间已经非常短了，如果已经非常短的话，再补充一个周期
        int exp;
        if (willExpire - nowTime > EXPIRE_TIME / 2) {
            exp = (int) (willExpire - nowTime);
        } else {
            exp = (int) (willExpire - nowTime) + EXPIRE_TIME;
        }
        v.expireTime = System.currentTimeMillis() + exp;//存储绝对过期时间
        return v;
    }
}
