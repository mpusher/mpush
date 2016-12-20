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
        return Arrays.stream(os).anyMatch(osName::contains);
    }

    public static ClientType find(String osName) {
        for (ClientType type : values()) {
            if (type.contains(osName.toLowerCase())) return type;
        }
        return UNKNOWN;
    }

    public static boolean isSameClient(String osNameA, String osNameB) {
        if (osNameA.equals(osNameB)) return true;
        return find(osNameA).contains(osNameB);
    }
}
