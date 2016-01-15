package com.shinemo.mpush.core.server;

import com.shinemo.mpush.api.Server;

/**
 * Created by ohun on 2016/1/8.
 */
public final class AdminServer implements Server {

    @Override
    public void start(Listener listener) {

    }

    @Override
    public void stop(Listener listener) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
}
