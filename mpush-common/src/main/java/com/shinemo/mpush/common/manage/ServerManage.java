package com.shinemo.mpush.common.manage;

import java.util.Collection;

import com.shinemo.mpush.tools.spi.SPI;

@SPI("connectionServerManage")
public interface ServerManage<T> {

	public void addOrUpdate(String fullPath, T application);

	public void remove(String fullPath);

	public Collection<T> getList();
	
}
