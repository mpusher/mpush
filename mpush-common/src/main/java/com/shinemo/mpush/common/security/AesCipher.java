package com.shinemo.mpush.common.security;

import com.shinemo.mpush.api.connection.Cipher;
import com.shinemo.mpush.tools.crypto.AESUtils;

/**
 * Created by ohun on 2015/12/28.
 */
public final class AesCipher implements Cipher {
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
}
