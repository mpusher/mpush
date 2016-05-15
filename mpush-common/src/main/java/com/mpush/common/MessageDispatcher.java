package com.mpush.common;

import com.mpush.api.MessageHandler;
import com.mpush.api.PacketReceiver;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.Packet;
import com.mpush.tools.Profiler;
import com.mpush.common.message.ErrorMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ohun on 2015/12/22.
 */
public final class MessageDispatcher implements PacketReceiver {
    public static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);
    private final Map<Byte, MessageHandler> handlers = new HashMap<>();

    public void register(Command command, MessageHandler handler) {
        handlers.put(command.cmd, handler);
    }


    @Override
    public void onReceive(Packet packet, Connection connection) {
    	MessageHandler handler = handlers.get(packet.cmd);
        try {
            if (handler != null) {
            	Profiler.enter("start handle:"+handler.getClass().getSimpleName());
                handler.handle(packet, connection);
            }
        } catch (Throwable throwable) {
            LOGGER.error("dispatch packet ex, packet={}, conn={}", packet, connection, throwable);
            ErrorMessage
                    .from(packet, connection)
                    .setErrorCode(ErrorCode.DISPATCH_ERROR)
                    .close();
        }finally{
        	if(handler!=null){
        		Profiler.release();
        	}
        }
    }
}
