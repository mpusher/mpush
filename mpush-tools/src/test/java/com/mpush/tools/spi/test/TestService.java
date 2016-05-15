package com.mpush.tools.spi.test;

import com.mpush.tools.spi.SPI;


@SPI("test1")
public interface TestService {

    public String sayHi(String name);

}
