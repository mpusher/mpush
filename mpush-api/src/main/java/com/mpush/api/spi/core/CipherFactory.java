package com.mpush.api.spi.core;

import com.mpush.api.connection.Cipher;
import com.mpush.api.spi.Factory;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public interface CipherFactory extends Factory<Cipher> {
    Cipher get();
}
