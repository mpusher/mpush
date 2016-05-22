package com.mpush.core.security;

import com.mpush.common.security.CipherBox;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by ohun on 2015/12/25.
 */
public class CipherBoxTest {

    @Test
    public void testGetPrivateKey() throws Exception {
    }

    @Test
    public void testGetPublicKey() throws Exception {
        for (int i = 0; i < 1000; i++) {
            byte[] clientKey = CipherBox.I.randomAESKey();
            byte[] serverKey = CipherBox.I.randomAESKey();
            byte[] sessionKey = CipherBox.I.mixKey(clientKey, serverKey);
            //System.out.println("clientKey:" + Arrays.toString(clientKey));
            //System.out.println("serverKey:" + Arrays.toString(serverKey));
            System.out.println("sessionKey:" + Arrays.toString(sessionKey));

        }

    }
}