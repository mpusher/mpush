package com.shinemo.mpush.api;

import java.nio.charset.Charset;

/**
 * Created by ohun on 2015/12/23.
 */
public interface Constants {
    Charset UTF_8 = Charset.forName("UTF-8");
    int MAX_PACKET_SIZE = 1024;
    int HEADER_LEN = 13;
    byte MAGIC_NUM1 = (byte) 33;
    byte MAGIC_NUM2 = (byte) 99;
}
