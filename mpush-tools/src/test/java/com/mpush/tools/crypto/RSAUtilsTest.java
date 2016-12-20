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

import com.mpush.tools.common.Pair;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public class RSAUtilsTest {
    String publicKey;
    String privateKey;

    @Before
    public void setUp() throws Exception {
        try {
            Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair(RSAUtils.RAS_KEY_SIZE);
            publicKey = RSAUtils.encodeBase64(pair.key);
            privateKey = RSAUtils.encodeBase64(pair.value);
            System.out.println("公钥: \n\r" + publicKey);
            System.out.println("私钥： \n\r" + privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetKeys() throws Exception {
        String source = "这是一行测试RSA数字签名的无意义文字";
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("加密后:\n" + new String(encodedData));
        byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData, privateKey);
        String target = new String(decodedData);
        System.out.println("解密后:\n" + target);
    }

    @Test
    public void testGetPrivateKey() throws Exception {
        System.out.println("公钥: \n\r" + publicKey);
        System.out.println("私钥： \n\r" + privateKey);
    }

    @Test
    public void testGetPublicKey() throws Exception {

    }

    @Test
    public void testSign() throws Exception {
        String source = "这是一行测试RSA数字签名的无意义文字";
        System.out.println("===========私钥加密——公钥解密=======================");
        System.out.println("原文字:\n" + source);
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);
        System.out.println("加密后:\n" + new String(encodedData));
        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);
        String target = new String(decodedData);
        System.out.println("解密后:\n" + target);
        System.out.println("============私钥签名——公钥验证签名===================");
        String sign = RSAUtils.sign(encodedData, privateKey);
        System.out.println("签名:\n" + sign);
        boolean status = RSAUtils.verify(encodedData, publicKey, sign);
        System.out.println("验证结果:\n" + status);
    }

    @Test
    public void test1() throws Exception {
        System.err.println("公钥加密——私钥解密");
        String source = "这是一行没有任何意义的文字，你看完了等于没看，不是吗？";
        System.out.println("\r加密前文字：\r\n" + source);
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("加密后文字：\r\n" + new String(encodedData));
        byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData, privateKey);
        String target = new String(decodedData);
        System.out.println("解密后文字: \r\n" + target + ", 明文长度:" + data.length + ", 密文长度:" + encodedData.length);
    }

    @Test
    public void testGetPrivateKey1() throws Exception {
        URL url = this.getClass().getResource("/");
        System.out.println(url.getPath());
        System.out.println(url.getFile());
    }

    @Test
    public void testEncryptByPublicKey() throws Exception {

    }

    @Test
    public void testDecryptByPrivateKey() throws Exception {

    }

    @Test
    public void testDecryptByPrivateKey1() throws Exception {

    }

    @Test
    public void testEncryptByPublicKey1() throws Exception {

    }

    @Test
    public void testEncryptByPrivateKey() throws Exception {

    }
}