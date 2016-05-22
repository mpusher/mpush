package com.mpush.core.handler;

import com.mpush.cache.redis.RedisKey;
import com.mpush.cache.redis.manager.RedisManager;
import com.mpush.tools.config.ConfigManager;
import com.mpush.tools.Jsons;
import com.mpush.tools.MPushUtil;
import com.mpush.zk.ZKClient;
import com.mpush.zk.ZKPath;
import com.mpush.zk.node.ZKServerNode;
import io.netty.channel.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@ChannelHandler.Sharable
public final class AdminHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminHandler.class);

    private static final String DOUBLE_END = "\r\n\r\n";

    private static final String EOL = "\r\n";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String request) throws Exception {
        Command command = Command.getCommand(request);
        ChannelFuture future = ctx.write(command.handler(request) + DOUBLE_END);
        if (command.equals(Command.QUIT)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("welcome to " + MPushUtil.getInetAddress() + "!" + EOL);
        ctx.write("It is " + new Date() + " now." + DOUBLE_END);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    public enum Command {
        HELP("help") {
            @Override
            public String handler(String request) {
                StringBuilder buf = new StringBuilder();
                buf.append("Command:" + EOL);
                buf.append("help:display all command." + EOL);
                buf.append("quit:exit checkHealth." + EOL);
                buf.append("scn:statistics connect num." + EOL);
                buf.append("rcs:remove current server zk info." + EOL);
                buf.append("scs:stop connection server.");
                return buf.toString();
            }
        },
        QUIT("quit") {
            @Override
            public String handler(String request) {
                return "have a good day!";
            }
        },
        SCN("scn") {
            @Override
            public String handler(String request) {
                Long value = RedisManager.I.zCard(RedisKey.getUserOnlineKey(MPushUtil.getExtranetAddress()));
                if (value == null) {
                    value = 0L;
                }
                return value.toString() + ".";
            }
        },
        RCS("rcs") {
            @Override
            public String handler(String request) {

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
                        LOGGER.info("delete connection server failed: required host:{}, but:{}", serverNode.getIp(), MPushUtil.getInetAddress());
                    }
                }
                if (removeSuccess) {
                    return "remove success.";
                } else {
                    return "remove false.";
                }
            }
        },
        SCS("scs") {
            @Override
            public String handler(String request) {
                return "not support now.";
            }
        };
        private final String cmd;

        public abstract String handler(String request);

        private Command(String cmd) {
            this.cmd = cmd;
        }

        public String getCmd() {
            return cmd;
        }

        public static Command getCommand(String request) {
            if (StringUtils.isNoneEmpty(request)) {
                for (Command command : Command.values()) {
                    if (command.getCmd().equals(request)) {
                        return command;
                    }
                }
            }
            return HELP;
        }
    }

}
