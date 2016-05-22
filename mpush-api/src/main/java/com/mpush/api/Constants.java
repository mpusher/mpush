package com.mpush.api;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 *
 * @author ohun@live.cn
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte[] EMPTY_BYTES = new byte[0];

    String HTTP_HEAD_READ_TIMEOUT = "readTimeout";

}
