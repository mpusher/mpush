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

package com.mpush.tools.crypto;


import com.mpush.api.Constants;

import java.util.Base64;

public class Base64Utils {

    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     *
     * @param base64 base64
     * @return 源二进制数据
     */
    public static byte[] decode(String base64) {
        return Base64.getDecoder().decode(base64.getBytes(Constants.UTF_8));
    }

    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     *
     * @param bytes base64
     * @return BASE64后的二进制数据
     */
    public static String encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes), Constants.UTF_8);
    }

}