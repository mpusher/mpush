package com.shinemo.mpush.push;

import com.shinemo.mpush.common.AbstractClient;
import com.shinemo.mpush.push.zk.listener.impl.ConnectionServerPathListener;

public class ConnClient extends AbstractClient{

    public ConnClient() {
        registerListener(new ConnectionServerPathListener());
    }

}
