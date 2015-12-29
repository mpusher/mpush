package com.shinemo.mpush.api.event;

/**
 * Created by ohun on 2015/12/29.
 */
public class KickUserEvent implements Event {
    public final String userId;
    public final String deviceId;
    public final String fromServer;

    public KickUserEvent(String userId, String deviceId, String fromServer) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.fromServer = fromServer;
    }
}
