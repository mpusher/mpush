/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.core.router;

import com.mpush.common.net.KickRemoteMsg;

/**
 * Created by ohun on 16/10/23.
 *
 * @author ohun@live.cn (夜色)
 */
class RedisKickRemoteMessage implements KickRemoteMsg {
    private String userId;
    private String deviceId;
    private String connId;
    private int clientType;
    private String targetServer;

    public RedisKickRemoteMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public RedisKickRemoteMessage setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public RedisKickRemoteMessage setConnId(String connId) {
        this.connId = connId;
        return this;
    }

    public RedisKickRemoteMessage setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }

    public RedisKickRemoteMessage setTargetServer(String targetServer) {
        this.targetServer = targetServer;
        return this;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getConnId() {
        return connId;
    }

    @Override
    public int getClientType() {
        return clientType;
    }

    @Override
    public String getTargetServer() {
        return targetServer;
    }

    @Override
    public String toString() {
        return "KickRemoteMsg{"
                + "userId='" + userId + '\''
                + ", deviceId='" + deviceId + '\''
                + ", connId='" + connId + '\''
                + ", clientType='" + clientType + '\''
                + ", targetServer='" + targetServer + '\''
                + '}';
    }
}
