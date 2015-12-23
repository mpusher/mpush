package com.shinemo.mpush.connection.netty;

import com.shinemo.mpush.core.thread.NamedThreadFactory;
import com.shinemo.mpush.core.thread.ThreadNameSpace;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class NettySharedHolder {

	public static final Timer timer = new HashedWheelTimer(new NamedThreadFactory(ThreadNameSpace.NETTY_TIMER));

	public static final ByteBufAllocator byteBufAllocator;

	static {
		byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
	}

}
