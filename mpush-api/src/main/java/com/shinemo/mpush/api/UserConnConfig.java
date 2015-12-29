package com.shinemo.mpush.api;

/**
 * Created by ohun on 2015/12/23.
 */
public final class UserConnConfig {
    private String host;
    private String osName;
    private String clientVer;

    public UserConnConfig(String host) {
        this.host = host;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
