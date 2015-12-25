package com.shinemo.mpush.core.message;

import com.shinemo.mpush.api.Message;

/**
 * Created by ohun on 2015/12/24.
 */
public class HandShakeMessage implements Message {
    public String deviceId;
    public String osName;
    public String osVersion;
    public String clientVersion;
    public String clientKey;
    public long timestamp;
}
