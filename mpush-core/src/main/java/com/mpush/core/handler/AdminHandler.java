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
import com.mpush.api.push.PushSender;
import com.mpush.api.service.Listener;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.user.UserManager;
import com.mpush.core.router.RouterCenter;
import com.mpush.core.server.AdminServer;
import com.mpush.tools.Jsons;
import com.mpush.tools.Utils;
import com.mpush.tools.common.Profiler;
import com.mpush.tools.config.CC;
import com.mpush.tools.config.ConfigManager;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.node.ZKServerNode;
import com.typesafe.config.ConfigRenderOptions;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public final class AdminHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminHandler.class);


    private static final String EOL = "\r\n";

    private static AdminServer adminServer;

    public AdminHandler(AdminServer adminServer) {
        this.adminServer = adminServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        Command command = Command.help;
        String arg = null;
        String[] args = null;
        if (request != null) {
            String[] cmd_args = request.split(" ");
            command = Command.toCmd(cmd_args[0].trim());
            if (cmd_args.length == 2) {
                arg = cmd_args[1];
            } else if (cmd_args.length > 2) {
                args = Arrays.copyOfRange(cmd_args, 1, cmd_args.length);
            }
        }
        try {
            Object result = args != null ? command.handler(ctx, args) : command.handler(ctx, arg);
            ChannelFuture future = ctx.writeAndFlush(result + EOL + EOL);
            if (command == Command.quit) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Throwable throwable) {
            ctx.writeAndFlush(throwable.getLocalizedMessage() + EOL + EOL);
            StringWriter writer = new StringWriter(1024);
            throwable.printStackTrace(new PrintWriter(writer));
            ctx.writeAndFlush(writer.toString());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("welcome to " + Utils.getLocalIp() + "!" + EOL);
        ctx.write("It is " + new Date() + " now." + EOL + EOL);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public enum Command {
        help {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                StringBuilder buf = new StringBuilder();
                buf.append("Option                               Description" + EOL);
                buf.append("------                               -----------" + EOL);
                buf.append("help                                 show help" + EOL);
                buf.append("quit                                 exit console mode" + EOL);
                buf.append("shutdown                             stop mpush server" + EOL);
                buf.append("restart                              restart mpush server" + EOL);
                buf.append("zk:<redis, cs ,gs>                   query zk node" + EOL);
                buf.append("count:<conn, online>                 count conn num or online user count" + EOL);
                buf.append("route:<uid>                          show user route info" + EOL);
                buf.append("push:<uid>, <msg>                    push test msg to client" + EOL);
                buf.append("conf:[key]                           show config info" + EOL);
                buf.append("monitor:[mxBean]                     show system monitor" + EOL);
                buf.append("profile:<1,0>                        enable/disable profile" + EOL);
                return buf.toString();
            }
        },
        quit {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                return "have a good day!";
            }
        },
        shutdown {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                ctx.writeAndFlush("try close connect server...");
                adminServer.getConnectionServer().stop(new Listener() {
                    @Override
                    public void onSuccess(Object... args) {
                        ctx.writeAndFlush("connect server close success" + EOL);
                        adminServer.stop(null);//这个一定要在System.exit之前调用，不然jvm 会卡死 @see com.mpush.bootstrap.Main#addHook
                        System.exit(0);
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        ctx.writeAndFlush("connect server close failure, msg=" + cause.getLocalizedMessage());
                    }
                });
                return null;
            }
        },
        restart {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                return "unsupported";
            }
        },
        zk {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                switch (args) {
                    case "redis":
                        return ZKClient.I.get(ZKPath.REDIS_SERVER.getRootPath());
                    case "cs":
                        return getNodeData(ZKPath.CONNECT_SERVER);
                    case "gs":
                        return getNodeData(ZKPath.GATEWAY_SERVER);

                }
                return "[" + args + "] unsupported, try help.";
            }

            private String getNodeData(ZKPath path) {
                List<String> rawData = ZKClient.I.getChildrenKeys(path.getRootPath());
                StringBuilder sb = new StringBuilder();
                for (String raw : rawData) {
                    sb.append(ZKClient.I.get(path.getFullPath(raw))).append('\n');
                }
                return sb.toString();
            }
        },
        count {
            @Override
            public Serializable handler(ChannelHandlerContext ctx, String args) {
                switch (args) {
                    case "conn":
                        return adminServer.getConnectionServer().getConnectionManager().getConnections().size();
                    case "online": {
                        return UserManager.I.getOnlineUserNum();
                    }

                }
                return "[" + args + "] unsupported, try help.";
            }
        },
        route {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                if (Strings.isNullOrEmpty(args)) return "please input userId";
                Set<RemoteRouter> routers = RouterCenter.I.getRemoteRouterManager().lookupAll(args);
                if (routers.isEmpty()) return "user [" + args + "] offline now.";
                return Jsons.toJson(routers);
            }
        },
        push {
            @Override
            public String handler(ChannelHandlerContext ctx, String... args) throws Exception {
                //Boolean success = PushSender.create().send(args[1], args[0], null).get(5, TimeUnit.SECONDS);

                return "unsupported";
            }
        },
        conf {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {
                if (Strings.isNullOrEmpty(args)) {
                    return CC.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true));
                }
                if (CC.cfg.hasPath(args)) {
                    return CC.cfg.getAnyRef(args).toString();
                }
                return "key [" + args + "] not find in config";
            }
        },
        profile {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) throws Exception {
                if (args == null || "0".equals(args)) {
                    Profiler.enable(false);
                    return "Profiler disabled";
                } else {
                    Profiler.enable(true);
                    return "Profiler enabled";
                }
            }
        },
        rcs {
            @Override
            public String handler(ChannelHandlerContext ctx, String args) {

                List<String> rawData = ZKClient.I.getChildrenKeys(ZKPath.CONNECT_SERVER.getRootPath());
                boolean removeSuccess = false;
                String localIp = ConfigManager.I.getLocalIp();
                for (String raw : rawData) {
                    String dataPath = ZKPath.CONNECT_SERVER.getFullPath(raw);
                    String data = ZKClient.I.get(dataPath);
                    ZKServerNode serverNode = Jsons.fromJson(data, ZKServerNode.class);
                    if (serverNode.getIp().equals(localIp)) {
                        ZKClient.I.remove(dataPath);
                        LOGGER.info("delete connection server success:{}", data);
                        removeSuccess = true;
                    } else {
                        LOGGER.info("delete connection server failed: required host:{}, but:{}", serverNode.getIp(), Utils.getLocalIp());
                    }
                }
                if (removeSuccess) {
                    return "removeAndClose success.";
                } else {
                    return "removeAndClose false.";
                }
            }
        };

        public Object handler(ChannelHandlerContext ctx, String... args) throws Exception {
            return "unsupported";
        }

        public Object handler(ChannelHandlerContext ctx, String args) throws Exception {
            return "unsupported";
        }

        public static Command toCmd(String cmd) {
            try {
                return Command.valueOf(cmd);
            } catch (Exception e) {
            }
            return help;
        }
    }
}
