package com.mpush.netty.util;


import com.mpush.tools.thread.NamedThreadFactory;
import com.mpush.tools.thread.ThreadNameSpace;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

public class NettySharedHolder {

    public static final Timer HASHED_WHEEL_TIMER = new HashedWheelTimer(new NamedThreadFactory(ThreadNameSpace.NETTY_TIMER));

    public static EventLoopGroup workerGroup = new NioEventLoopGroup();

}
