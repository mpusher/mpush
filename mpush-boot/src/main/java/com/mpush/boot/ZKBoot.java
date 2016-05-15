package com.mpush.boot;

import com.google.common.collect.Lists;
import com.mpush.zk.ZKClient;
import com.mpush.zk.listener.ZKDataChangeListener;
import com.mpush.zk.listener.ZKRedisNodeListener;

import java.util.List;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ZKBoot extends BootJob {
    private List<ZKDataChangeListener> dataChangeListeners = Lists.newArrayList();

    public ZKBoot() {
        registerListener(new ZKRedisNodeListener());
    }

    public void registerListener(ZKDataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    private void registerListeners() {
        for (ZKDataChangeListener listener : dataChangeListeners) {
            ZKClient.I.registerListener(listener);
        }
    }

    private void initListenerData() {
        for (ZKDataChangeListener listener : dataChangeListeners) {
            listener.initData();
        }
    }


    @Override
    public void run() {
        registerListeners();
        initListenerData();
        next();
    }
}
