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
public final class DESUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DESUtils.class);
    private static byte[] iv = {8, 7, 6, 5, 5, 6, 7, 8};

    public static byte[] encryptDES(byte[] encryptBytes, String encryptKey) {
        return encryptDES(encryptBytes, 0, encryptBytes.length, encryptKey);
    }

    public static byte[] encryptDES(byte[] encryptBytes, int offset, int length, String encryptKey) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(Constants.UTF_8), "DES");
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            return cipher.doFinal(encryptBytes, offset, length);
        } catch (Exception e) {
            LOGGER.error("encryptDES ex, decryptKey=" + encryptKey, e);
        }
        return Constants.EMPTY_BYTES;
    }

    public static byte[] decryptDES(byte[] byteMi, String decryptKey) {
        return decryptDES(byteMi, 0, byteMi.length, decryptKey);
    }

    public static byte[] decryptDES(byte[] byteMi, int offset, int length, String decryptKey) {
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(Constants.UTF_8), "DES");
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            return cipher.doFinal(byteMi, offset, length);
        } catch (Exception e) {
            LOGGER.error("decryptDES ex, decryptKey=" + decryptKey, e);
        }
        return Constants.EMPTY_BYTES;
    }
}
