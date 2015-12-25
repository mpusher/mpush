package com.shinemo.mpush.core.security;

/**
 * Created by ohun on 2015/12/25.
 */
public class ReusableToken {
    public transient String tokenId;
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public String desKey;
    public long expireTime;

}
