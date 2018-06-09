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

package com.mpush.core.handler;

import com.google.common.base.Strings;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.MPushServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.common.Profiler;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigTools;
import com.typesafe.config.ConfigRenderOptions;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ChannelHandler.Sharable
public final class AdminHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminHandler.class);

    private static final String EOL = "\r\n";

    private final LocalDateTime startTime = LocalDateTime.now();

    private final Map<String, OptionHandler> optionHandlers = new HashMap<>();

    private final OptionHandler unsupported_handler = (_1, _2) -> "unsupported option";

    private final MPushServer mPushServer;

    public AdminHandler(MPushServer mPushServer) {
        this.mPushServer = mPushServer;
        init();
    }

    public void init() {
        register("help", (ctx, args) ->
                "Option                               Description" + EOL +
                        "------                               -----------" + EOL +
                        "help                                 show help" + EOL +
                        "quit                                 exit console mode" + EOL +
                        "shutdown                             stop mpush server" + EOL +
                        "restart                              restart mpush server" + EOL +
                        "zk:<redis, cs ,gs>                   query zk node" + EOL +
                        "count:<conn, online>                 count conn num or online user count" + EOL +
                        "route:<uid>                          show user route info" + EOL +
                        "push:<uid>, <msg>                    push test msg to client" + EOL +
                        "conf:[key]                           show config info" + EOL +
                        "monitor:[mxBean]                     show system monitor" + EOL +
                        "profile:<1,0>                        enable/disable profile" + EOL
        );

        register("quit", (ctx, args) -> "have a good day!");

        register("shutdown", (ctx, args) -> {
            new Thread(() -> System.exit(0)).start();
            return "try close connect server...";
        });

        register("count", (ctx, args) -> {
            switch (args) {
                case "conn":
                    return mPushServer.getConnectionServer().getConnectionManager().getConnNum();
                case "online": {
                    return mPushServer.getRouterCenter().getUserEventConsumer().getUserManager().getOnlineUserNum();
                }

            }
            return "[" + args + "] unsupported, try help.";
        });

        register("route", (ctx, args) -> {
            if (Strings.isNullOrEmpty(args)) return "please input userId";
            Set<RemoteRouter> routers = mPushServer.getRouterCenter().getRemoteRouterManager().lookupAll(args);
            if (routers.isEmpty()) return "user [" + args + "] offline now.";
            return Jsons.toJson(routers);
        });

        register("conf", (ctx, args) -> {
            if (Strings.isNullOrEmpty(args)) {
                return CC.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true));
            }
            if (CC.cfg.hasPath(args)) {
                return CC.cfg.getAnyRef(args).toString();
            }
            return "key [" + args + "] not find in config";
        });

        register("profile", (ctx, args) -> {
            if (args == null || "0".equals(args)) {
                Profiler.enable(false);
                return "Profiler disabled";
            } else {
                Profiler.enable(true);
                return "Profiler enabled";
            }
        });
    }


    private void register(String option, OptionHandler handler) {
        optionHandlers.put(option, handler);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        String option = "help";
        String arg = null;
        String[] args = null;
        if (!Strings.isNullOrEmpty(request)) {
            String[] cmd_args = request.split(" ");
            option = cmd_args[0].trim().toLowerCase();
            if (cmd_args.length == 2) {
                arg = cmd_args[1];
            } else if (cmd_args.length > 2) {
                args = Arrays.copyOfRange(cmd_args, 1, cmd_args.length);
            }
        }
        try {
            Object result = optionHandlers.getOrDefault(option, unsupported_handler).handle(ctx, arg);
            ChannelFuture future = ctx.writeAndFlush(result + EOL + EOL);
            if (option.equals("quit")) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Throwable throwable) {
            ctx.writeAndFlush(throwable.getLocalizedMessage() + EOL + EOL);
            StringWriter writer = new StringWriter(1024);
            throwable.printStackTrace(new PrintWriter(writer));
            ctx.writeAndFlush(writer.toString());
        }
        LOGGER.info("receive admin command={}", request);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("Welcome to MPush Console [" + ConfigTools.getLocalIp() + "]!" + EOL);
        ctx.write("since " + startTime + " has running " + startTime.until(LocalDateTime.now(), ChronoUnit.HOURS) + "(h)" + EOL + EOL);
        ctx.write("It is " + new Date() + " now." + EOL + EOL);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public interface OptionHandler {
        Object handle(ChannelHandlerContext ctx, String args) throws Exception;
    }
}
