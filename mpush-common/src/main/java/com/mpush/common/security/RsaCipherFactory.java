package com.mpush.common.security;

import com.mpush.api.connection.Cipher;
import com.mpush.api.spi.core.CipherFactory;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public class RsaCipherFactory implements CipherFactory {
    private static final RsaCipher RSA_CIPHER = RsaCipher.create();

    @Override
    public Cipher get() {
        return RSA_CIPHER;
    }
}
