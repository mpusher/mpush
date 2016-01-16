package com.shinemo.mpush.tools.zk;


import org.apache.curator.utils.ZKPaths;

public enum ZKPath {
    REDIS_SERVER("/redis", "machine", "redis注册的地方"),
    CONNECTION_SERVER("/cs/hosts", "machine", "connection server服务器应用注册的路径"),
    PUSH_SERVER("/ps/hosts", "machine", "push server服务器应用注册的路径"),
    GATEWAY_SERVER("/gs/hosts", "machine", "gateway server服务器应用注册的路径");

    ZKPath(String path, String name, String desc) {
        this.path = path;
        this.name = name;
    }

    private final String path;
    private final String name;

    public String getPath() {
        return path;
    }

    public String getWatchPath() {
        return path + ZKPaths.PATH_SEPARATOR + name;
    }

    //根据从zk中获取的app的值，拼装全路径
    public String getFullPath(String nodeName) {
        return path + ZKPaths.PATH_SEPARATOR + nodeName;
    }

    public static void main(String[] args) {
        String test = "/cs/%s/kick";

        System.out.println(String.format(test, "10.1.10.65"));
    }

}
