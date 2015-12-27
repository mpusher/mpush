package com.shinemo.mpush.core.security;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created by ohun on 2015/12/25.
 */
public class CipherManagerTest {

    @Test
    public void testGetPrivateKey() throws Exception {
        CipherManager.INSTANCE.getPrivateKey();
    }

    @Test
    public void testGetPublicKey() throws Exception {
        for (int i = 0; i < 1000; i++) {
            byte[] clientKey = CipherManager.INSTANCE.randomAESKey();
            byte[] serverKey = CipherManager.INSTANCE.randomAESKey();
            byte[] sessionKey = CipherManager.INSTANCE.mixKey(clientKey, serverKey);
            System.out.println("clientKey:" + Arrays.toString(clientKey));
            System.out.println("serverKey:" + Arrays.toString(serverKey));
            System.out.println("sessionKey:" + Arrays.toString(sessionKey));

        }

    }
}