package com.mpush.common.security;

import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.crypto.RSAUtils;

import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by ohun on 2015/12/24.
 */
public final class CipherBox {
    public int aesKeyLength = ConfigCenter.holder.aesKeyLength();
    public static final CipherBox INSTANCE = new CipherBox();
    private SecureRandom random = new SecureRandom();
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public RSAPrivateKey getPrivateKey() {
        if (privateKey == null) {
            String key = ConfigCenter.holder.privateKey();
            try {
                privateKey = (RSAPrivateKey) RSAUtils.decodePrivateKey(key);
            } catch (Exception e) {
                throw new RuntimeException("load private key ex, key=" + key, e);
            }
        }
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        if (publicKey == null) {
            String key = ConfigCenter.holder.publicKey();
            try {
                publicKey = (RSAPublicKey) RSAUtils.decodePublicKey(key);
            } catch (Exception e) {
                throw new RuntimeException("load public key ex, key=" + key, e);
            }
        }
        return publicKey;
    }

    public byte[] randomAESKey() {
        byte[] bytes = new byte[aesKeyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] randomAESIV() {
        byte[] bytes = new byte[aesKeyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] mixKey(byte[] clientKey, byte[] serverKey) {
        byte[] sessionKey = new byte[aesKeyLength];
        for (int i = 0; i < aesKeyLength; i++) {
            byte a = clientKey[i];
            byte b = serverKey[i];
            int sum = Math.abs(a + b);
            int c = (sum % 2 == 0) ? a ^ b : b ^ a;
            sessionKey[i] = (byte) c;
        }
        return sessionKey;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public RsaCipher getRsaCipher() {
        return new RsaCipher(getPrivateKey(), getPublicKey());
    }
}
