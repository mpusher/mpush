package com.shinemo.mpush.tools.redis;

import java.util.Date;
import java.util.List;

import com.shinemo.mpush.tools.redis.listener.MessageListener;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.redis.manage.RedisGroupManage;
import com.shinemo.mpush.tools.redis.manage.RedisManage;
import com.shinemo.mpush.tools.zk.ServerApp;
import com.shinemo.mpush.tools.zk.manage.ServerManage;

public class RedisGroupManageTest {

    ServerApp app = new ServerApp(InetAddressUtil.getInetAddress(), "3000");
    ServerManage manage = new ServerManage(app);
    List<RedisGroup> groupList = null;

    RedisNode node = new RedisNode("127.0.0.1", 6379, "ShineMoIpo");
    RedisNode node2 = new RedisNode("127.0.0.1", 6380, "ShineMoIpo");

    @Before
    public void init() {
        manage.start();
        groupList = RedisGroupManage.instance.getGroupList();
    }

    @Test
    public void testGetRedisGroup() {
        for (RedisGroup group : groupList) {
            for (RedisNode node : group.getRedisNodeList()) {
                System.out.println(group + ToStringBuilder.reflectionToString(node, ToStringStyle.MULTI_LINE_STYLE));
            }

        }
    }

    @Test
    public void testAdd() {
        User user = RedisManage.get("huang2", User.class);
        if (user == null) {
            user = new User("hi", 10, new Date());
            RedisManage.set("huang2", user);
            user = RedisManage.get("huang2", User.class);
        }
        System.out.println(ToStringBuilder.reflectionToString(user, ToStringStyle.MULTI_LINE_STYLE));

        User nowUser = RedisUtil.get(node, "huang2", User.class);
        System.out.println("node1:" + ToStringBuilder.reflectionToString(nowUser));

        nowUser = RedisUtil.get(node2, "huang2", User.class);
        System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));

        RedisManage.del("huang2");

        nowUser = RedisUtil.get(node2, "huang2", User.class);
        if (nowUser == null) {
            System.out.println("node2 nowUser is null");
        } else {
            System.out.println("node2:" + ToStringBuilder.reflectionToString(nowUser));
        }

        nowUser = RedisUtil.get(node, "huang2", User.class);
        System.out.println("node:" + ToStringBuilder.reflectionToString(nowUser));

    }

    @Test
    public void testPub() {
        for (int i = 0; i < 20; i++) {
            User user = new User("pub" + i, 10, new Date());
            RedisManage.publish("channel1", user);
            RedisManage.publish("channel2", user);
        }
    }

    @Test
    public void testSub() {
        RedisManage.subscribe(new MessageListener() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.printf("on message channel=%s, message=%s%n", channel, message);
            }
        }, "channel1", "channel2");
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
