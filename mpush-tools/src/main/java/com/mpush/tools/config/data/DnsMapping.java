package com.mpush.tools.config.data;


import java.util.Objects;

public class DnsMapping {
    private String ip;
    private int port;

    public DnsMapping(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public static DnsMapping parse(String addr) {
        String[] host_port = Objects.requireNonNull(addr, "dns mapping can not be null")
                .split(":");
        if (host_port.length == 1) {
            return new DnsMapping(host_port[0], 80);
        } else {
            return new DnsMapping(host_port[0], Integer.valueOf(host_port[1]));
        }
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
