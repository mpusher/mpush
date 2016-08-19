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

package com.mpush.api.router;

import java.util.Arrays;

/**
 * Created by ohun on 16/8/18.
 *
 * @author ohun@live.cn (夜色)
 */
public enum ClientType {
    MOBILE(1, "android", "ios"),
    PC(2, "windows", "mac", "linux"),
    WEB(3, "web", "h5"),
    UNKNOWN(-1);

    public final int type;
    public final String[] os;

    ClientType(int type, String... os) {
        this.type = type;
        this.os = os;
    }

    public boolean contains(String osName) {
        return Arrays.stream(os).anyMatch(s -> s.equalsIgnoreCase(osName));
    }

    public static boolean isSameClient(String osName1, String osName2) {
        if (osName1.equals(osName2)) return true;
        return find(osName1).contains(osName2);
    }

    public static ClientType find(String osName) {
        for (ClientType type : values()) {
            if (type.contains(osName)) return type;
        }
        return UNKNOWN;
    }
}
