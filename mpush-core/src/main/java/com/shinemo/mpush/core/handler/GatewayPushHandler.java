package com.shinemo.mpush.core.handler;

import com.shinemo.mpush.api.connection.Connection;
import com.shinemo.mpush.common.message.ErrorMessage;
import com.shinemo.mpush.common.message.OkMessage;
import com.shinemo.mpush.common.message.PushMessage;
import com.shinemo.mpush.common.message.gateway.GatewayPushMessage;
import com.shinemo.mpush.api.protocol.Packet;
import com.shinemo.mpush.api.router.Router;
import com.shinemo.mpush.common.handler.BaseMessageHandler;
import com.shinemo.mpush.common.router.RouterCenter;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Created by ohun on 2015/12/30.
 */
public final class GatewayPushHandler extends BaseMessageHandler<GatewayPushMessage> {
    @Override
    public GatewayPushMessage decode(Packet packet, Connection connection) {
        return new GatewayPushMessage(packet, connection);
    }

    @Override
    public void handle(final GatewayPushMessage message) {
        Router<?> router = RouterCenter.INSTANCE.lookup(message.userId);
        if (router.getRouteType() == Router.RouterType.LOCAL) {
            Connection connection = (Connection) router.getRouteValue();
            PushMessage pushMessage = new PushMessage(message.content, connection);
            pushMessage.send(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        OkMessage.from(message).send();
                    } else {
                        ErrorMessage
                                .from(message)
                                .setCode((byte) 1)
                                .setReason("push to client error")
                                .send();
                    }
                }
            });
        } else {
            // TODO: 2015/12/30 send message to other server
        }
    }
}
