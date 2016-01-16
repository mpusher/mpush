package com.shinemo.mpush.cs.manage;

import java.util.Collection;

import com.shinemo.mpush.tools.spi.SPI;

@SPI("redisServerManage")
public interface ServerManage<T> {

	public void addOrUpdate(String fullPath, T application);

	public void remove(String fullPath);

	public Collection<T> getList();
	
}
