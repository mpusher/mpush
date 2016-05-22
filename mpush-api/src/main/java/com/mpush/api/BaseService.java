package com.mpush.api;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yxx on 2016/5/19.
 *
 * @author ohun@live.cn
 */
public abstract class BaseService implements Service {

    protected final AtomicBoolean started = new AtomicBoolean();

    @Override
    public void init() {
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    public final void start() {
        start(null);
    }

    public final void stop() {
        start(null);
    }
}
