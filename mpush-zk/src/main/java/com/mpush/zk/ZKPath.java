package com.mpush.zk;


import org.apache.curator.utils.ZKPaths;

public enum ZKPath {
    REDIS_SERVER("/redis", "machine", "redis注册的地方"),
    CONNECT_SERVER("/cs/hosts", "machine", "connection server服务器应用注册的路径"),
    PUSH_SERVER("/ps/hosts", "machine", "push server服务器应用注册的路径"),
    GATEWAY_SERVER("/gs/hosts", "machine", "gateway server服务器应用注册的路径");

    ZKPath(String root, String name, String desc) {
        this.root = root;
        this.name = name;
    }

    private final String root;
    private final String name;

    public String getRootPath() {
        return root;
    }

    public String getNodePath() {
        return root + ZKPaths.PATH_SEPARATOR + name;
    }

    //根据从zk中获取的app的值，拼装全路径
    public String getFullPath(String tail) {
        return root + ZKPaths.PATH_SEPARATOR + tail;
    }

}
