package com.mpush.push;

import com.mpush.common.AbstractClient;
import com.mpush.push.zk.listener.ConnectZKListener;

public class ConnectClient extends AbstractClient{

    public ConnectClient() {
        registerListener(new ConnectZKListener());
    }

}
