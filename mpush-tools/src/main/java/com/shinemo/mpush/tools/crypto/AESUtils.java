package com.shinemo.mpush.tools.crypto;

import com.shinemo.mpush.tools.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ohun on 2015/12/25.
 */
public final class AESUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);
    public static final int AES_KEY_LENGTH = 16;
    public static final String KEY_ALGORITHM = "AES";
    public static final String PKCS_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static byte[] encrypt(byte[] data, byte[] encryptKey, byte[] iv) {
        return encrypt(data, 0, data.length, encryptKey, iv);
    }

    public static byte[] decrypt(byte[] data, byte[] decryptKey, byte[] iv) {
        return decrypt(data, 0, data.length, decryptKey, iv);
    }

    public static byte[] encrypt(byte[] data, int offset, int length, byte[] encryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey, KEY_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(PKCS_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            return cipher.doFinal(data, offset, length);
        } catch (Exception e) {
            LOGGER.error("encrypt ex, decryptKey=" + encryptKey, e);
        }
        return Constants.EMPTY_BYTES;
    }

    public static byte[] decrypt(byte[] data, int offset, int length, byte[] decryptKey, byte[] iv) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey, KEY_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(PKCS_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            return cipher.doFinal(data, offset, length);
        } catch (Exception e) {
            LOGGER.error("decrypt ex, decryptKey=" + decryptKey, e);
        }
        return Constants.EMPTY_BYTES;
    }
}
