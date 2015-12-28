package com.shinemo.mpush.core.security;

import com.shinemo.mpush.api.SessionContext;

/**
 * Created by ohun on 2015/12/25.
 */
public class ReusableSession {
    public transient String sessionId;
    public long expireTime;
    public SessionContext sessionContext;

}
