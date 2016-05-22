package com.mpush.tools.config.data;

/**
 * redis 相关的配置信息
 */
public class RedisServer {
    public String host;
    public int port;
    public String password;

    public RedisServer() {
    }

    public RedisServer(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RedisServer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                '}';
    }
}
