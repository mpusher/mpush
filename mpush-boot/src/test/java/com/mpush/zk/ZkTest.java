package com.mpush.zk;

import org.junit.Test;


public class ZkTest {

    @Test
    public void remove() {
        ZKClient zkRegister = ZKClient.I;

        zkRegister.remove("/");

    }

}
