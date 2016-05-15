package com.mpush.boot;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public abstract class BootJob {
    private BootJob next;

    abstract void run();

    public void next() {
        if (next != null) {
            next.run();
        }
    }

    public BootJob setNext(BootJob next) {
        this.next = next;
        return next;
    }
}
