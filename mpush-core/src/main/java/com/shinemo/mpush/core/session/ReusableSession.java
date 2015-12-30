package com.shinemo.mpush.core.session;

import com.shinemo.mpush.api.connection.SessionContext;

/**
 * Created by ohun on 2015/12/25.
 */
public final class ReusableSession {
    public String sessionId;
    public long expireTime;
    public SessionContext sessionContext;

}
