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

package com.mpush.common.router;

/**
 * Created by ohun on 16/10/23.
 *
 * 剔除远程信息
 *
 * @author ohun@live.cn (夜色)
 */
public interface KickRemoteMsg {
    String getUserId();

    String getDeviceId();

    String getConnId();

    int getClientType();

    String getTargetServer();

    int getTargetPort();

    default boolean isTargetMachine(String host, int port) {
        return this.getTargetPort() == port
                && this.getTargetServer().equals(host);
    }
}
