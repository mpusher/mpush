package com.shinemo.mpush.tools.zk;

import java.io.Serializable;

public class ServerApp implements Serializable {

    private static final long serialVersionUID = 5495972321679092837L;

    private final String ip;
    private final int port;

    public ServerApp(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
