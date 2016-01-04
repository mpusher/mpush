package com.shinemo.mpush.core.router;

/**
 * Created by ohun on 2016/1/4.
 */
public class KickRemoteMsg {
    public String userId;
    public String deviceId;
    public String srcServer;

    @Override
    public String toString() {
        return "KickRemoteMsg{" +
                "userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", srcServer='" + srcServer + '\'' +
                '}';
    }
}
