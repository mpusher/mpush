package com.shinemo.mpush.tools.crypto;

import com.shinemo.mpush.tools.Constants;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ohun on 2015/12/25.
 */
public class DESUtilsTest {

    @Test
    public void testEncryptDES() throws Exception {
        String data = "似的士大夫士大夫士大夫首发式发生士大夫";
        System.out.println("原文：\n" + data);
        String key = RandomStringUtils.randomAscii(8);
        byte[] d1 = DESUtils.encryptDES(data.getBytes(Constants.UTF_8), key);
        System.out.println("加密后：\n" + new String(d1));
        byte[] d2 = DESUtils.decryptDES(d1, key);
        System.out.println("解密后：\n" + new String(d2, Constants.UTF_8));
    }

    @Test
    public void testDecryptDES() throws Exception {

    }
}