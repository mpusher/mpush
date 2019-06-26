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

package com.mpush.core.session;

import com.mpush.api.connection.SessionContext;
import com.mpush.common.security.AesCipher;

/**
 * Created by ohun on 2015/12/25.
 *
 * 可复用会话
 *
 * @author ohun@live.cn
 */
public final class ReusableSession {
    public String sessionId;
    public long expireTime;
    public SessionContext context;

    public static String encode(SessionContext context) {
        StringBuffer sb = new StringBuffer();
        sb.append(context.osName).append(',');
        sb.append(context.osVersion).append(',');
        sb.append(context.clientVersion).append(',');
        sb.append(context.deviceId).append(',');
        sb.append(context.cipher);
        return sb.toString();
    }

    public static ReusableSession decode(String value) {
        String[] array = value.split(",");
        if (array.length != 6) return null;
        SessionContext context = new SessionContext();
        context.osName = array[0];
        context.osVersion = array[1];
        context.clientVersion = array[2];
        context.deviceId = array[3];
        byte[] key = AesCipher.toArray(array[4]);
        byte[] iv = AesCipher.toArray(array[5]);
        if (key == null || iv == null) return null;
        context.cipher = new AesCipher(key, iv);
        ReusableSession session = new ReusableSession();
        session.context = context;
        return session;
    }
}
