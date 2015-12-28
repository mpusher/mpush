package com.shinemo.mpush.netty.util;


import com.shinemo.mpush.tools.thread.NamedThreadFactory;
import com.shinemo.mpush.tools.thread.ThreadNameSpace;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class NettySharedHolder {

    public static final Timer timer = new HashedWheelTimer(new NamedThreadFactory(ThreadNameSpace.NETTY_TIMER));


}
