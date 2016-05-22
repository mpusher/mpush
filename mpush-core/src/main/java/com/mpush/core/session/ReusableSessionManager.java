package com.mpush.core.session;

import com.mpush.api.connection.SessionContext;
import com.mpush.cache.redis.RedisKey;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.Strings;
import com.mpush.tools.config.CC;
import com.mpush.tools.crypto.MD5Utils;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public final class ReusableSessionManager {
    public static final ReusableSessionManager INSTANCE = new ReusableSessionManager();
    private int expiredTime = CC.mp.core.session_expired_time;

    public boolean cacheSession(ReusableSession session) {
        String key = RedisKey.getSessionKey(session.sessionId);
        RedisManager.I.set(key, ReusableSession.encode(session.context), expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId) {
        String key = RedisKey.getSessionKey(sessionId);
        String value = RedisManager.I.get(key, String.class);
        if (Strings.isBlank(value)) return null;
        return ReusableSession.decode(value);
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.context = context;
        session.sessionId = MD5Utils.encrypt(context.deviceId + now);
        session.expireTime = now + expiredTime * 1000;
        return session;
    }
}
