/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.api.connection;


import com.mpush.api.router.ClientClassifier;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public final class SessionContext {
    public String osName;
    public String osVersion;
    public String clientVersion;
    public String deviceId;
    public String userId;
    public String tags;
    public int heartbeat = 10000;// 10s
    public Cipher cipher;
    private byte clientType;

    public void changeCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    public SessionContext setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public SessionContext setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public SessionContext setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public SessionContext setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public SessionContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public boolean handshakeOk() {
        return deviceId != null && deviceId.length() > 0;
    }

    public int getClientType() {
        if (clientType == 0) {
            clientType = (byte) ClientClassifier.I.getClientType(osName);
        }
        return clientType;
    }

    public boolean isSecurity() {
        return cipher != null;
    }

    @Override
    public String toString() {
        if (userId == null && deviceId == null) {
            return "";
        }

        return "{" +
                "osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", heartbeat=" + heartbeat +
                '}';
    }
}
