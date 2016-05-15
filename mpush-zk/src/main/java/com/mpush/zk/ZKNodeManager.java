package com.mpush.zk;

import com.mpush.tools.spi.SPI;

import java.util.Collection;

@SPI("connectionServerManage")
public interface ZKNodeManager<T> {

    void addOrUpdate(String fullPath, T application);

    void remove(String fullPath);

    Collection<T> getList();

}
