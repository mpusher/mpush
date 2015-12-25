package com.shinemo.mpush.api;

import com.shinemo.mpush.api.protocol.Command;
import com.shinemo.mpush.api.protocol.Packet;

/**
 * Created by ohun on 2015/12/22.
 */
public interface Request {

    Command getCommand();

    Packet getMessage();

    Connection getConnection();

    Response getResponse();
}
