package com.shinemo.mpush.connection.netty.task;

import com.shinemo.mpush.api.Connection;

public interface ScanTask {
	
	/**
	 * 
	 * @param now 扫描触发的时间点
	 * @param client 当前扫描到的连接
	 */
	public void visit(long now,Connection conn);

}
