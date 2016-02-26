package com.shinemo.mpush.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.core.handler.AdminHandler;
import com.shinemo.mpush.netty.server.NettyServer;
import com.shinemo.mpush.tools.thread.threadpool.ThreadPoolManager;

public final class AdminServer extends NettyServer {

	private static final Logger log = LoggerFactory.getLogger(AdminServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    
    private final int port;
    
    private AdminHandler adminHandler = new AdminHandler();
	
	public AdminServer(int port) {
		super(port);
		this.port = port;
	}

	@Override
	public void start(Listener listener) {
		if (!serverState.compareAndSet(State.Initialized, State.Starting)) {
			throw new IllegalStateException("Server already started or have not init");
		}
		createNioServer(listener);
	}

	@Override
	public ChannelHandler getChannelHandler() {
		return adminHandler;
	}

	private void createNioServer(final Listener listener) {
		NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, ThreadPoolManager.bossExecutor);
		NioEventLoopGroup workerGroup = new NioEventLoopGroup(0, ThreadPoolManager.workExecutor);
		
		createServer(listener, bossGroup, workerGroup, NioServerSocketChannel.class);
	}
	
	private void createServer(final Listener listener, EventLoopGroup boss, EventLoopGroup work, Class<? extends ServerChannel> clazz) {
        this.bossGroup = boss;
        this.workerGroup = work;
        try {

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(clazz)
             .childHandler(new ChannelInitializer<SocketChannel>() {
            	@Override
            	protected void initChannel(SocketChannel ch) throws Exception {
            		 ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            		 ch.pipeline().addLast("decoder", new StringDecoder());
                     ch.pipeline().addLast("encoder", new StringEncoder());
                     ch.pipeline().addLast("handler", getChannelHandler());
            	} 
			  });


            /***
             * 绑定端口并启动去接收进来的连接
             */
            ChannelFuture f = b.bind(port).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                    	log.info("server start success on:" + port);
                        if (listener != null) listener.onSuccess();
                    } else {
                    	log.error("server start failure on:" + port);
                        if (listener != null) listener.onFailure("start server failure");
                    }
                }
            });
//            f.await();
            if (f.isSuccess()) {
            	serverState.set(State.Started);
                /**
                 * 这里会一直等待，直到socket被关闭
                 */
                f.channel().closeFuture().sync();
            }

        } catch (Exception e) {
            log.error("server start exception", e);
            if (listener != null) listener.onFailure("start server ex=" + e.getMessage());
            throw new RuntimeException("server start exception, port=" + port, e);
        } finally {
            /***
             * 优雅关闭
             */
            stop(null);
        }
    }

}
