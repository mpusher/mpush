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

import com.mpush.tools.common.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public final class AESUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);
    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_PADDING = "AES/CBC/PKCS5Padding";


    public static SecretKey getSecretKey(byte[] seed) throws Exception {
        SecureRandom secureRandom = new SecureRandom(seed);
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(secureRandom);
        return keyGenerator.generateKey();
    }

    public static byte[] encrypt(byte[] data, byte[] encryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey, KEY_ALGORITHM);
        return encrypt(data, zeroIv, key);
    }

    public static byte[] decrypt(byte[] data, byte[] decryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey, KEY_ALGORITHM);
        return decrypt(data, zeroIv, key);
    }

    public static byte[] encrypt(byte[] data, IvParameterSpec zeroIv, SecretKeySpec keySpec) {
        try {
            Profiler.enter("time cost on [aes encrypt]: data length=" + data.length);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.error("AES encrypt ex, iv={}, key={}",
                    Arrays.toString(zeroIv.getIV()),
                    Arrays.toString(keySpec.getEncoded()), e);
            throw new CryptoException("AES encrypt ex", e);
        } finally {
            Profiler.release();
        }
    }

    public static byte[] decrypt(byte[] data, IvParameterSpec zeroIv, SecretKeySpec keySpec) {
        try {
            Profiler.enter("time cost on [aes decrypt]: data length=" + data.length);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, zeroIv);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.error("AES decrypt ex, iv={}, key={}",
                    Arrays.toString(zeroIv.getIV()),
                    Arrays.toString(keySpec.getEncoded()), e);
            throw new CryptoException("AES decrypt ex", e);
        } finally {
            Profiler.release();
        }
    }
}
