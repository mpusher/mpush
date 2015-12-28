package com.shinemo.mpush.core.security;

import com.shinemo.mpush.tools.Pair;
import com.shinemo.mpush.tools.crypto.AESUtils;
import com.shinemo.mpush.tools.crypto.RSAUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/24.
 */
public class CipherBox {
    public static final CipherBox INSTANCE = new CipherBox();
    private SecureRandom random = new SecureRandom();

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public CipherBox() {
        init();
    }

    public void init() {
        readFromFile();
        if (publicKey == null || privateKey == null) {
            Pair<RSAPublicKey, RSAPrivateKey> pair = RSAUtils.genKeyPair();
            //生成公钥和私钥
            publicKey = pair.key;
            privateKey = pair.value;
            //模
            String modulus = publicKey.getModulus().toString();
            //公钥指数
            String public_exponent = publicKey.getPublicExponent().toString();
            //私钥指数
            String private_exponent = privateKey.getPrivateExponent().toString();
            //使用模和指数生成公钥和私钥
            publicKey = RSAUtils.getPublicKey(modulus, public_exponent);
            privateKey = RSAUtils.getPrivateKey(modulus, private_exponent);
            writeToFile();
        }
    }

    private void writeToFile() {
        try {
            String publicKeyStr = RSAUtils.encodeBase64(publicKey);
            String privateKeyStr = RSAUtils.encodeBase64(privateKey);
            String path = this.getClass().getResource("/").getPath();
            FileOutputStream out = new FileOutputStream(new File(path, "private.key"));
            out.write(privateKeyStr.getBytes());
            out.close();
            out = new FileOutputStream(new File(path, "public.key"));
            out.write(publicKeyStr.getBytes());
            out.close();
            System.out.println("write key=" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
        try {
            InputStream in = this.getClass().getResourceAsStream("/private.key");
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();
            privateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(new String(buffer));
            in = this.getClass().getResourceAsStream("/public.key");
            in.read(buffer);
            in.close();
            publicKey = (RSAPublicKey) RSAUtils.decodePublicKey(new String(buffer));
            System.out.println("save privateKey=" + privateKey);
            System.out.println("save publicKey=" + publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RSAPrivateKey getPrivateKey() {
        if (privateKey == null) init();
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        if (publicKey == null) init();
        return publicKey;
    }

    public byte[] randomAESKey() {
        byte[] bytes = new byte[AESUtils.AES_KEY_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] randomAESIV() {
        byte[] bytes = new byte[AESUtils.AES_KEY_LENGTH];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] mixKey(byte[] clientKey, byte[] serverKey) {
        byte[] sessionKey = new byte[AESUtils.AES_KEY_LENGTH];
        for (int i = 0; i < AESUtils.AES_KEY_LENGTH; i++) {
            byte a = clientKey[i];
            byte b = serverKey[i];
            int sum = Math.abs(a + b);
            int c = (sum % 2 == 0) ? a ^ b : b ^ a;
            sessionKey[i] = (byte) c;
        }
        return sessionKey;
    }

    public RsaCipher getRsaCipher() {
        return new RsaCipher(privateKey, publicKey);
    }
}
