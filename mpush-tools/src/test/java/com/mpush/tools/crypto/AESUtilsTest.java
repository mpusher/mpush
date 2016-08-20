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
import org.junit.Test;

import java.util.Random;

/**
 * Created by ohun on 2015/12/25.
 */
public class AESUtilsTest {

    @Test
    public void testEncryptDES() throws Exception {
        String data = "似的士大夫士大夫士大夫首发式发生士大夫";
        System.out.println("原文：\n" + data);
        byte[] key = new byte[16];
        new Random().nextBytes(key);
        byte[] d1 = AESUtils.encrypt(data.getBytes(Constants.UTF_8), key,key);
        System.out.println("加密后：\n" + new String(d1));
        byte[] d2 = AESUtils.decrypt(d1, key,key);
        System.out.println("解密后：\n" + new String(d2, Constants.UTF_8));
    }

    @Test
    public void testDecryptDES() throws Exception {

    }
}