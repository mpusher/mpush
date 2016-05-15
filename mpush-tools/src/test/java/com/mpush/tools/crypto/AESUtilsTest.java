package com.mpush.tools.crypto;

import com.mpush.tools.Constants;
import org.junit.Test;

import java.util.Random;

/**
 * Created by ohun on 2015/12/25.
 */
public class AESUtilsTest {

    @Test
    public void testEncryptDES() throws Exception {
        String data = "似的士大夫士大夫士大夫首发式发生士大夫";
        System.out.println("原文：\n" + data);
        byte[] key = new byte[16];
        new Random().nextBytes(key);
        byte[] d1 = AESUtils.encrypt(data.getBytes(Constants.UTF_8), key,key);
        System.out.println("加密后：\n" + new String(d1));
        byte[] d2 = AESUtils.decrypt(d1, key,key);
        System.out.println("解密后：\n" + new String(d2, Constants.UTF_8));
    }

    @Test
    public void testDecryptDES() throws Exception {

    }
}