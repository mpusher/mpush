package com.shinemo.mpush.core.session;

import com.shinemo.mpush.api.connection.SessionContext;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.config.ConfigCenter;
import com.shinemo.mpush.tools.crypto.MD5Utils;
import com.shinemo.mpush.tools.redis.manage.RedisManage;

/**
 * Created by ohun on 2015/12/25.
 */
public final class ReusableSessionManager {
    public static final ReusableSessionManager INSTANCE = new ReusableSessionManager();
    private int expiredTime = ConfigCenter.holder.sessionExpiredTime();

    public boolean cacheSession(ReusableSession session) {
        RedisManage.set(session.sessionId, ReusableSession.encode(session.context), expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId) {
        String value = RedisManage.get(sessionId, String.class);
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
