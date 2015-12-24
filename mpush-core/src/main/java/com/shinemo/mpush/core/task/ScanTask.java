package com.shinemo.mpush.core.task;

import com.shinemo.mpush.core.NettyConnection;

public interface ScanTask {
	
	/**
	 * 
	 * @param now 扫描触发的时间点
	 * @param client 当前扫描到的连接
	 */
	public void visit(long now,NettyConnection client);

}
