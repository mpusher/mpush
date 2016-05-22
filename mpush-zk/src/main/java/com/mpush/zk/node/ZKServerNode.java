package com.mpush.zk.node;


import com.mpush.tools.Jsons;
import com.mpush.tools.MPushUtil;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigManager;
import com.mpush.zk.ZKPath;

/**
 * 系统配置
 */
public class ZKServerNode implements ZKNode {

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
                CC.mp.net.connect_server_port,
                ConfigManager.I.getPublicIp(),
                ZKPath.CONNECT_SERVER.getNodePath());
    }

    public static ZKServerNode gsNode() {
        return new ZKServerNode(MPushUtil.getLocalIp(),
                CC.mp.net.gateway_server_port,
                null,
                ZKPath.GATEWAY_SERVER.getNodePath());
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
                "host='" + ip + '\'' +
                ", port=" + port +
                ", extranetIp='" + extranetIp + '\'' +
                ", zkPath='" + zkPath + '\'' +
                '}';
    }

    @Override
    public String encode() {
        return Jsons.toJson(this);
    }
}
