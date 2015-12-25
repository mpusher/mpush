package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public class RouterInfo {
    private String serverIp;
    private String osName;
    private String clientVer;

    public RouterInfo(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getClientVer() {
        return clientVer;
    }

    public void setClientVer(String clientVer) {
        this.clientVer = clientVer;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
