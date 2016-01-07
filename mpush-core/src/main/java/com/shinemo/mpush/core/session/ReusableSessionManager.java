package com.shinemo.mpush.core.session;

import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.crypto.MD5Utils;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.Map;

/**
 * Created by ohun on 2015/12/25.
 */
public final class ReusableSessionManager {
    public static final ReusableSessionManager INSTANCE = new ReusableSessionManager();
    private static final int EXPIRE_TIME = 86400;

    public boolean cacheSession(ReusableSession session) {
        RedisManage.set(session.sessionId, session.encode(), EXPIRE_TIME);
        return true;
    }

    public ReusableSession getSession(String sessionId) {
        String value = RedisManage.get(sessionId, String.class);
        if (Strings.isBlank(value)) return null;
        ReusableSession session = new ReusableSession();
        try {
            session.decode(value);
        } catch (Exception e) {
            return null;
        }
        return session;
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.context = context;
        session.sessionId = MD5Utils.encrypt(context.deviceId + now);
        session.expireTime = now + EXPIRE_TIME * 1000;
        return session;
    }

}
