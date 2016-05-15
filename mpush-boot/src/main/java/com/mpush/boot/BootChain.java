package com.mpush.boot;

/**
 * Created by yxx on 2016/5/15.
 *
 * @author ohun@live.cn
 */
public class BootChain {
    private BootJob first = first();

    public void run() {
        first.run();
    }

    public static BootChain chain() {
        return new BootChain();
    }

    private BootJob first() {
        return new BootJob() {
            @Override
            public void run() {
                next();
            }
        };
    }

    public BootJob boot() {
        return first;
    }
}
