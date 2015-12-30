package com.shinemo.mpush.api.connection;

import io.netty.channel.Channel;

/**
 * Created by ohun on 2015/12/30.
 */
public interface ConnectionManager {
    Connection get(Channel channel);

    void remove(Channel channel);

    void add(Connection connection);
}
