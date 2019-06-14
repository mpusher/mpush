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

/**
 * 缓存key
 */
public final class CacheKeys {
    //用户路由
    private static final String USER_PREFIX = "mp:ur:";
    //可复用session
    private static final String SESSION_PREFIX = "mp:rs:";

    private static final String FAST_CONNECTION_DEVICE_PREFIX = "mp:fcd:";
    //在线用户列表
    private static final String ONLINE_USER_LIST_KEY_PREFIX = "mp:oul:";

    public static final String SESSION_AES_KEY = "mp:sa";
    public static final String SESSION_AES_SEQ_KEY = "mp:sas";
    public static final String PUSH_TASK_PREFIX = "mp:pt";


    // 根据用户id，存储别名、标签、在线设备列表、离线消息
    private static final String USER_INFO_KEY_PREFIX = "mp:info:";
    // 存储 别名 对应的 用户id
    public static final String ALIAS_INFO_KEY_PREFIX = "mp:info:alias";
    // 存储 标签 对应的 用户id
    public static final String TAGS_INFO_KEY_PREFIX = "mp:info:tags";
    //离线消息
    private static final String MSG_KEY_PREFIX = "mp:msg:";


    public static String getUserInfoKey(String userId) {
        return USER_INFO_KEY_PREFIX + userId;
    }
    public static String getMsgKey(String msgId) {
        return MSG_KEY_PREFIX + msgId;
    }

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
