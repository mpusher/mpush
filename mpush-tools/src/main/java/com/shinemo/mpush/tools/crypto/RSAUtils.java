package com.shinemo.mpush.tools.crypto;

import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
 * * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 */
public class RSAUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);
    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 生成公钥和私钥
     *
     * @throws NoSuchAlgorithmException
     */
    public static Pair<RSAPublicKey, RSAPrivateKey> genKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(1024);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return Pair.of(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("getKeys ex ", e);
        }
        return null;
    }


    public static String encodeBase64(Key key) throws Exception {
        return Base64Utils.encode(key.getEncoded());
    }

    public static PrivateKey decodePrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64Utils.decode(key);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    public static PublicKey decodePublicKey(String publicKey) throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(x509KeySpec);
    }


    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(decodePrivateKey(privateKey));
        signature.update(data);
        return Base64Utils.encode(signature.sign());
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(decodePublicKey(publicKey));
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign));
    }


    /**
     * 使用模和指数生成RSA公钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 指数
     * @return
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
        }
        return null;
    }

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus  模
     * @param exponent 指数
     * @return
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
            return null;
        }
    }

    /**
     * 公钥加密
     *
     * @param data
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            // 模长
            int key_len = publicKey.getModulus().bitLength() / 8;
            // 加密数据长度 <= 模长-11
            //如果明文长度大于模长-11则要分组加密
            return doFinal(cipher, data, key_len - 11);
        } catch (Exception e) {
            LOGGER.error("encryptByPublicKey ex", e);
        }
        return Constants.EMPTY_BYTES;
    }

    /**
     * 私钥解密
     *
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            //模长
            int key_len = privateKey.getModulus().bitLength() / 8;
            //如果密文长度大于模长则要分组解密
            return doFinal(cipher, data, key_len);
        } catch (Exception e) {
            LOGGER.error("decryptByPrivateKey ex", e);
        }
        return Constants.EMPTY_BYTES;
    }

    /**
     * 注意：RSA加密明文最大长度117字节，解密要求密文最大长度为128字节，所以在加密和解密的过程中需要分块进行。
     *
     * @param cipher
     * @param data
     * @return
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static byte[] doFinal(Cipher cipher, byte[] data, int key_len) throws BadPaddingException, IllegalBlockSizeException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = data.length, offSet = 0;
        byte[] tmp;
        while (inputLen > 0) {
            tmp = cipher.doFinal(data, offSet, Math.min(key_len, inputLen));
            out.write(tmp, 0, tmp.length);
            offSet += key_len;
            inputLen -= key_len;
        }
        return out.toByteArray();
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey key = decodePrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key);
        return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data, String publicKey) throws Exception {
        PublicKey key = decodePublicKey(publicKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return doFinal(cipher, data, MAX_DECRYPT_BLOCK);
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        PublicKey key = decodePublicKey(publicKey);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        PrivateKey key = decodePrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return doFinal(cipher, data, MAX_ENCRYPT_BLOCK);
    }

    public static void main(String[] args) throws Exception {
        Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair();
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
        RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
        RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
        //加密后的密文
        byte[] mi = RSAUtils.encryptByPublicKey(ming, pubKey);
        System.out.println("密文：" + new String(mi, Constants.UTF_8));
        //解密后的明文
        ming = RSAUtils.decryptByPrivateKey(mi, priKey);
        System.out.println("解密：" + new String(ming, Constants.UTF_8));
    }
}