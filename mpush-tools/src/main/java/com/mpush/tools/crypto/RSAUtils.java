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
import com.mpush.tools.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA公钥/私钥/签名工具包
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 */
public final class RSAUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

    /**
     * 密钥位数
     */
    public static final int RAS_KEY_SIZE = 1024;

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 填充方式
     */
    public static final String KEY_ALGORITHM_PADDING = "RSA/ECB/PKCS1Padding";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 128 - 11;


    /**
     * 生成公钥和私钥
     *
     * @param rsaKeySize key size
     *
     * @return 公钥和私钥
     */
    public static Pair<RSAPublicKey, RSAPrivateKey> genKeyPair(int rsaKeySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(rsaKeySize);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return Pair.of(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("getKeys ex ", e);
        }
        return null;
    }

    /**
     * 编码密钥，便于存储
     *
     * @param key 密钥
     * @return base64后的字符串
     * @throws Exception Exception
     */
    public static String encodeBase64(Key key) throws Exception {
        return Base64Utils.encode(key.getEncoded());
    }

    /**
     * 从字符串解码私钥
     *
     * @param key 密钥
     * @return base64后的字符串
     * @throws Exception Exception
     */
    public static PrivateKey decodePrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64Utils.decode(key);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 从字符串解码公钥
     *
     * @param publicKey 公钥
     * @return 公钥
     * @throws Exception Exception
     */
    public static PublicKey decodePublicKey(String publicKey) throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(x509KeySpec);
    }


    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return 私钥
     * @throws Exception Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(decodePrivateKey(privateKey));
        signature.update(data);
        return Base64Utils.encode(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名
     * @return 是否通过校验
     * @throws Exception Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(decodePublicKey(publicKey));
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign));
    }


    /**
     * 使用模和指数生成RSA公钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，
     * 不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 指数
     * @return 公钥
     */
    public static RSAPublicKey getPublicKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            LOGGER.error("getPublicKey ex modulus={}, exponent={}", modulus, exponent, e);
            throw new CryptoException("Get PublicKey ex", e);
        }
    }

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，
     * 不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 指数
     * @return 私钥
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            LOGGER.error("getPrivateKey ex modulus={}, exponent={}", modulus, exponent, e);
            throw new CryptoException("Get PrivateKey ex", e);
        }
    }

    /**
     * 公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后的值
     */
    public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            // 模长
            int key_len = publicKey.getModulus().bitLength() / 8;
            // 加密数据长度 <= 模长-11
            //如果明文长度大于模长-11则要分组加密
            return doFinal(cipher, data, key_len - 11);
        } catch (Exception e) {
            LOGGER.error("encryptByPublicKey ex", e);
            throw new CryptoException("RSA encrypt ex", e);
        }
    }

    /**
     * 私钥解密
     *
     * @param data       待加密数据
     * @param privateKey 私钥
     * @return 解密后的值
     */
    public static byte[] decryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            //模长
            int key_len = privateKey.getModulus().bitLength() / 8;
            //如果密文长度大于模长则要分组解密
            return doFinal(cipher, data, key_len);
        } catch (Exception e) {
            LOGGER.error("decryptByPrivateKey ex", e);
            throw new CryptoException("RSA decrypt ex", e);
        }
    }

    /**
     * 注意：RSA加密明文最大长度117字节，
     * 解密要求密文最大长度为128字节，
     * 所以在加密和解密的过程中需要分块进行。
     *
     * @param cipher 密钥
     * @param data   待处理的数据
     * @return 处理后的值
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static byte[] doFinal(Cipher cipher, byte[] data, int key_len) throws BadPaddingException, IllegalBlockSizeException {
        int inputLen = data.length, offset = 0;
        byte[] tmp;
        ByteArrayOutputStream out = new ByteArrayOutputStream(getTmpArrayLength(inputLen));
        while (inputLen > 0) {
            tmp = cipher.doFinal(data, offset, Math.min(key_len, inputLen));
            out.write(tmp, 0, tmp.length);
            offset += key_len;
            inputLen -= key_len;
        }
        return out.toByteArray();
    }

    private static int getTmpArrayLength(int L) {
        int S = MAX_DECRYPT_BLOCK;
        while (S < L) S <<= 1;
        return S;
    }

    /**
     * 私钥解密
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return 解密后的值
     * @throws Exception Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        PrivateKey key = decodePrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * 公钥解密
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @return 解密后的值
     * @throws Exception Exception
     */
    public static byte[] decryptByPublicKey(byte[] data, String publicKey) throws Exception {
        PublicKey key = decodePublicKey(publicKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * 公钥加密
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return 加密后的值
     * @throws Exception Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        PublicKey key = decodePublicKey(publicKey);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
    }

    /**
     * 私钥加密
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return 加密后的值
     * @throws Exception Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        PrivateKey key = decodePrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
    }

    private static void test() {
        Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair(RAS_KEY_SIZE);
        //生成公钥和私钥
        RSAPublicKey publicKey = pair.key;
        RSAPrivateKey privateKey = pair.value;
        //模
        String modulus = publicKey.getModulus().toString();
        //公钥指数
        String public_exponent = publicKey.getPublicExponent().toString();
        //私钥指数
        String private_exponent = privateKey.getPrivateExponent().toString();
        //明文
        byte[] ming = "123456789".getBytes(Constants.UTF_8);
        System.out.println("明文：" + new String(ming, Constants.UTF_8));
        //使用模和指数生成公钥和私钥
        RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
        RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
        System.out.println("privateKey=" + priKey);
        System.out.println("publicKey=" + pubKey);
        //加密后的密文
        byte[] mi = RSAUtils.encryptByPublicKey(ming, pubKey);
        System.out.println("密文：" + new String(mi, Constants.UTF_8));
        //解密后的明文
        ming = RSAUtils.decryptByPrivateKey(mi, priKey);
        System.out.println("解密：" + new String(ming, Constants.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        int keySize = RAS_KEY_SIZE;
        if (args.length > 0) keySize = Integer.parseInt(args[0]);
        if (keySize < RAS_KEY_SIZE) keySize = RAS_KEY_SIZE;
        Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair(keySize);
        //生成公钥和私钥
        RSAPublicKey publicKey = pair.key;
        RSAPrivateKey privateKey = pair.value;

        System.out.println("key generate success!");

        System.out.println("privateKey=" + RSAUtils.encodeBase64(privateKey));
        System.out.println("publicKey=" + RSAUtils.encodeBase64(publicKey));

        //明文
        byte[] ming = "这是一段测试文字。。。。".getBytes(Constants.UTF_8);
        System.out.println("明文:" + new String(ming, Constants.UTF_8));

        //加密后的密文
        byte[] mi = RSAUtils.encryptByPublicKey(ming, publicKey);
        System.out.println("密文:" + new String(mi, Constants.UTF_8));
        //解密后的明文
        ming = RSAUtils.decryptByPrivateKey(mi, privateKey);
        System.out.println("解密:" + new String(ming, Constants.UTF_8));
    }
}