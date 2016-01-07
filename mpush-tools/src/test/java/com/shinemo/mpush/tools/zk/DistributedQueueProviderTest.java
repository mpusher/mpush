package com.shinemo.mpush.tools.zk;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.zk.manage.ServerAppManage;
import com.shinemo.mpush.tools.zk.manage.ServerManage;
import com.shinemo.mpush.tools.zk.queue.Provider;

public class DistributedQueueProviderTest {

    private ServerApp app = new ServerApp("10.1.10.64", 3000);

    private ServerManage manage = new ServerManage(app, PathEnum.CONNECTION_SERVER);

    @Before
    public void setup() {
        manage.start();
    }

    @Test
    public void test() throws Exception {

        Iterator<ServerApp> iterator = ServerAppManage.instance.getAppList().iterator();

        List<Provider<ServerApp>> providers = Lists.newArrayList();
        while (iterator.hasNext()) {
            ServerApp app = iterator.next();
            if (!app.getIp().equals(this.app.getIp())) {
                Provider<ServerApp> provider = new Provider<>(PathEnum.GATEWAY_SERVER.getPathByIp(app.getIp()), ServerApp.class);
                providers.add(provider);
                provider.start();
            }
        }

        for (int i = 0; i < 10; i++) {
            providers.get(0).put(new ServerApp("hi" + i, 1000));
        }

        Thread.sleep(20000);

    }

    @After
    public void close() {
        manage.close();
    }

}
