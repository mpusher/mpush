package com.mpush.common.security;

import com.mpush.api.connection.Cipher;
import com.mpush.tools.Profiler;
import com.mpush.tools.crypto.AESUtils;


/**
 * Created by ohun on 2015/12/28.
 */
public final class AesCipher implements Cipher {
    public final byte[] key;
    public final byte[] iv;

    public AesCipher(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    public byte[] decrypt(byte[] data) {
    	try{
    		Profiler.enter("start aes decrypt");
    		return AESUtils.decrypt(data, key, iv);
    	}finally{
    		Profiler.release();
    	}
    }

    @Override
    public byte[] encrypt(byte[] data) {
    	try{
    		Profiler.enter("start encrypt");
    		return AESUtils.encrypt(data, key, iv);
    	}finally{
    		Profiler.release();
    	}
    }

    @Override
    public String toString() {
        return toString(key) + ',' + toString(iv);
    }

    public String toString(byte[] a) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i != 0) b.append('|');
            b.append(a[i]);
        }
        return b.toString();
    }

    public static byte[] toArray(String str) {
        String[] a = str.split("\\|");
        if (a.length != CipherBox.INSTANCE.getAesKeyLength()) {
            return null;
        }
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = Byte.parseByte(a[i]);
        }
        return bytes;
    }
}
