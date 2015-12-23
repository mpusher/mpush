package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public class RouterInfo {
    private String ip;
    private String os;
    private String clientVer;

    public RouterInfo(String ip) {
        this.ip = ip;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getClientVer() {
        return clientVer;
    }

    public void setClientVer(String clientVer) {
        this.clientVer = clientVer;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
