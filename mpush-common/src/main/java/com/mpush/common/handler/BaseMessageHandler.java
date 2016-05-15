package com.mpush.common.handler;


import com.mpush.api.Message;
import com.mpush.api.MessageHandler;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.tools.Profiler;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public abstract class BaseMessageHandler<T extends Message> implements MessageHandler {
    public abstract T decode(Packet packet, Connection connection);

    public abstract void handle(T message);

    public void handle(Packet packet, Connection connection) {
        T t = decode(packet, connection);
        if (t != null) {
            try {
                Profiler.enter("start handle");
                handle(t);
            } finally {
                Profiler.release();
            }
        }
    }
}
