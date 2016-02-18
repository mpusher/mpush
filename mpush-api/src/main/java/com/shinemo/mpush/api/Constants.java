package com.shinemo.mpush.api;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    int HEADER_LEN = 13;

    byte CRYPTO_FLAG = 0x01;
    byte COMPRESS_FLAG = 0x02;
    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";

}
