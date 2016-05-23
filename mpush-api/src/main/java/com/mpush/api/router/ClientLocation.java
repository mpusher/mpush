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

package com.mpush.api.router;

import com.mpush.api.connection.SessionContext;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public final class ClientLocation {

    /**
     * 长链接所在的机器IP
     */
    private String host;

    /**
     * 客户端系统类型
     */
    private String osName;

    /**
     * 客户端版本
     */
    private String clientVersion;

    /**
     * 客户端设备ID
     */
    private String deviceId;


    public String getDeviceId() {
        return deviceId;
    }

    public ClientLocation setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }


    public String getHost() {
        return host;
    }

    public ClientLocation setHost(String host) {
        this.host = host;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public ClientLocation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public ClientLocation setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public static ClientLocation from(SessionContext context) {
        ClientLocation config = new ClientLocation();
        config.osName = context.osName;
        config.clientVersion = context.clientVersion;
        config.deviceId = context.deviceId;
        return config;
    }

    @Override
    public String toString() {
        return "ClientLocation{" +
                "host='" + host + '\'' +
                ", osName='" + osName + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
