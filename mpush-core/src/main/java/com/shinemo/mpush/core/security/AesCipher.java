package com.shinemo.mpush.core.security;

import com.shinemo.mpush.api.Cipher;
import com.shinemo.mpush.tools.Strings;
import com.shinemo.mpush.tools.crypto.AESUtils;
import com.shinemo.mpush.tools.crypto.Base64Utils;

/**
 * Created by ohun on 2015/12/28.
 */
public class AesCipher implements Cipher {
    private final byte[] key;
    private final byte[] iv;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return AESUtils.decrypt(data, key, iv);
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return AESUtils.encrypt(data, key, iv);
    }


    public static String encodeCipher(AesCipher aesCipher) {
        try {
            return Base64Utils.encode(aesCipher.key) + " " + Base64Utils.encode(aesCipher.iv);
        } catch (Exception e) {
            return Strings.EMPTY;
        }
    }

    public static AesCipher decodeCipher(String keys) {
        if (Strings.isBlank(keys)) return null;
        String[] array = keys.split(" ");
        if (array.length != 2) return null;
        try {
            byte[] key = Base64Utils.decode(array[0]);
            byte[] iv = Base64Utils.decode(array[1]);
            if (key.length == AESUtils.AES_KEY_LENGTH && iv.length == AESUtils.AES_KEY_LENGTH) {
                return new AesCipher(key, iv);
            }
        } catch (Exception e) {
        }
        return null;
    }
}
