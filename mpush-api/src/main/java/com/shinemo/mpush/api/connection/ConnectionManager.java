package com.shinemo.mpush.api.connection;

import java.util.List;

import io.netty.channel.Channel;

/**
 * Created by ohun on 2015/12/30.
 */
public interface ConnectionManager {

    Connection get(Channel channel);

    void remove(Channel channel);

    void add(Connection connection);

	List<Connection> getConnections();

	void init();

    void destroy();
}
