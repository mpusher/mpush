package com.mpush.zk;

import com.mpush.zk.ZKClient;
import org.junit.Test;


public class ZkTest {

    @Test
    public void remove() {
        ZKClient zkRegister = new ZKClient();

        zkRegister.init();

        zkRegister.remove("/");

    }

}
