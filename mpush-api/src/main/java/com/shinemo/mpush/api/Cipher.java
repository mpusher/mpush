package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/28.
 */
public interface Cipher {

    byte[] decrypt(byte[] data);

    byte[] encrypt(byte[] data);

}
