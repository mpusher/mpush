package com.shinemo.mpush.core.task;

import com.shinemo.mpush.core.NettyConnection;

public interface ScanTask {
	
	public void visit(long now,NettyConnection client);

}
