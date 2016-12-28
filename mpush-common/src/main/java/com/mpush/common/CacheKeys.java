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

package com.mpush.common;

public final class CacheKeys {

    private static final String USER_PREFIX = "mp:ur:";//用户路由

    private static final String SESSION_PREFIX = "mp:rs:";//可复用session

    private static final String FAST_CONNECTION_DEVICE_PREFIX = "mp:fcd:";

    private static final String ONLINE_USER_LIST_KEY_PREFIX = "mp:oul:";//在线用户列表

    public static final String SESSION_AES_KEY = "mp:sa";
    public static final String SESSION_AES_SEQ_KEY = "mp:sas";
    public static final String PUSH_TASK_PREFIX = "mp:pt";

    public static String getUserRouteKey(String userId) {
        return USER_PREFIX + userId;
    }

    public static String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    public static String getDeviceIdKey(String deviceId) {
        return FAST_CONNECTION_DEVICE_PREFIX + deviceId;
    }

    public static String getOnlineUserListKey(String publicIP) {
        return ONLINE_USER_LIST_KEY_PREFIX + publicIP;
    }

    public static String getPushTaskKey(String taskId) {
        return PUSH_TASK_PREFIX + taskId;
    }

}
