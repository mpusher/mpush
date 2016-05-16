package com.mpush.zk;


import com.mpush.tools.MPushUtil;
import com.mpush.tools.config.ConfigCenter;

/**
 * 系统配置
 */
public class ZKServerNode {

    private String ip;

    private int port;

    private String extranetIp;

    private transient String zkPath;

    public ZKServerNode() {
    }

    public ZKServerNode(String ip, int port, String extranetIp, String zkPath) {
        this.ip = ip;
        this.port = port;
        this.extranetIp = extranetIp;
        this.zkPath = zkPath;
    }

    public static ZKServerNode csNode() {
        return new ZKServerNode(MPushUtil.getLocalIp(),
                ConfigCenter.I.connectionServerPort(),
                MPushUtil.getExtranetAddress(),
                ZKPath.CONNECTION_SERVER.getWatchPath());
    }

    public static ZKServerNode gsNode() {
        return new ZKServerNode(MPushUtil.getLocalIp(),
                ConfigCenter.I.gatewayServerPort(),
                MPushUtil.getExtranetAddress(),
                ZKPath.GATEWAY_SERVER.getWatchPath());
    }

    public String getIp() {
        return ip;
    }

    public ZKServerNode setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ZKServerNode setPort(int port) {
        this.port = port;
        return this;
    }

    public String getExtranetIp() {
        return extranetIp;
    }

    public ZKServerNode setExtranetIp(String extranetIp) {
        this.extranetIp = extranetIp;
        return this;
    }

    public String getZkPath() {
        return zkPath;
    }

    public ZKServerNode setZkPath(String zkPath) {
        this.zkPath = zkPath;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZKServerNode that = (ZKServerNode) o;

        if (port != that.port) return false;
        return ip != null ? ip.equals(that.ip) : that.ip == null;

    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ZKServerNode{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", extranetIp='" + extranetIp + '\'' +
                ", zkPath='" + zkPath + '\'' +
                '}';
    }
}
