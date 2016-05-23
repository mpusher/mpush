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

package com.mpush.cache.redis;

public final class RedisKey {

    private static final String USER_PREFIX = "mp_u_";

    private static final String SESSION_PREFIX = "mp_s_";

    private static final String FAST_CONNECTION_DEVICE_PREFIX = "mp_f_c_d_";

    private static final String USER_ONLINE_KEY = "mp_u_ol_";

    private static final String CONN_NUM_ = "mp_cn_";

    public static final String getUserKey(String userId) {
        return USER_PREFIX + userId;
    }

    public static final String getSessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    //for fast connection test
    public static final String getDeviceIdKey(String deviceId) {
        return FAST_CONNECTION_DEVICE_PREFIX + deviceId;
    }

    public static final String getUserOnlineKey(String extranetAddress) {
        return USER_ONLINE_KEY + extranetAddress;
    }

//    public static final String getConnNum(String extranetAddress) {
//        return CONN_NUM_ + extranetAddress;
//    }


}
