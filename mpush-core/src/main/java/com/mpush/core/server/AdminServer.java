/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.server;

import com.mpush.core.MPushServer;
import com.mpush.core.handler.AdminHandler;
import com.mpush.netty.server.NettyTCPServer;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.ThreadNames;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public final class AdminServer extends NettyTCPServer {

    private AdminHandler adminHandler;

    private MPushServer mPushServer;

    public AdminServer(MPushServer mPushServer) {
        super(CC.mp.net.admin_server_port);
        this.mPushServer = mPushServer;
    }

    @Override
    public void init() {
        super.init();
        this.adminHandler = new AdminHandler(mPushServer);
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        super.initPipeline(pipeline);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return adminHandler;
    }

    @Override
    protected ChannelHandler getDecoder() {
        return new StringDecoder();
    }

    @Override
    protected ChannelHandler getEncoder() {
        return new StringEncoder();
    }

    @Override
    protected int getWorkThreadNum() {
        return 1;
    }

    @Override
    protected String getBossThreadName() {
        return ThreadNames.T_ADMIN_BOSS;
    }

    @Override
    protected String getWorkThreadName() {
        return ThreadNames.T_ADMIN_WORKER;
    }
}
