package com.shinemo.mpush.tools.zk;

import java.util.List;

import com.shinemo.mpush.tools.ConfigCenter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.shinemo.mpush.tools.Constants;
import com.shinemo.mpush.tools.InetAddressUtil;
import com.shinemo.mpush.tools.Jsons;
import com.shinemo.mpush.tools.redis.RedisGroup;
import com.shinemo.mpush.tools.redis.RedisNode;

public class ZkUtilTest {

    private ZkUtil zkUtil;

    @Before
    public void setUp() throws Exception {
        ConfigCenter.INSTANCE.init();
        zkUtil = ZkUtil.instance;

    }

    @Test
    public void test() {

        String dubbo = zkUtil.get("/dubbo");
        System.out.println(dubbo);

        List<String> child = zkUtil.getChildrenKeys("/dubbo");
        System.out.println(ToStringBuilder.reflectionToString(child, ToStringStyle.JSON_STYLE));

        zkUtil.registerPersist("/hi", "hello world");

        zkUtil.registerEphemeral("/huang", "hi");
        zkUtil.registerEphemeralSequential("/huang2");

        String huang = zkUtil.get("/huang");
        System.out.println(huang);

        String huang2 = zkUtil.get("/huang2");
        System.out.println(huang2);

    }

    @Test
    public void getTest() {
        String value = zkUtil.get("/hi");
        System.out.println(value);
    }

    /**
     * 注册机器到/mpush/allhost 目录下边
     */
    @Test
    public void testRegister() {

        String path = "/" + zkUtil.getZkConfig().getNamespace();

        String prefix = Constants.ZK_REGISTER_PREFIX_NAME;

        List<String> hosts = zkUtil.getChildrenKeys(path);

        System.out.println("before register");

        for (int i = 0; i < hosts.size(); i++) {
            String value = zkUtil.get(hosts.get(i));
            System.out.println(hosts.get(i) + "," + value);
        }

        System.out.println("start register");

        zkUtil.registerEphemeralSequential(path + "/" + prefix);

        zkUtil.registerEphemeralSequential(path + "/" + prefix);

        hosts = zkUtil.getChildrenKeys(path);

        for (int i = 0; i < hosts.size(); i++) {
            String value = zkUtil.get(path + "/" + hosts.get(i));
            System.out.println(hosts.get(i) + "," + value);
        }

        System.out.println("end register");

    }

    @Test
    public void testLocalIp() {
        System.out.println(InetAddressUtil.getInetAddress());

    }

    @Test
    public void testRegisterIp() {
        String localIp = InetAddressUtil.getInetAddress();
        ServerApp app = new ServerApp(localIp, "3000");
        zkUtil.registerPersist("/" + localIp, Jsons.toJson(app));
        String value = zkUtil.get("/" + localIp);
        System.out.println(value);
    }

    @Test
    public void testRemove() {
        zkUtil.remove("/");
    }

    @Test
    public void testAddKickOff() {
        String localIp = InetAddressUtil.getInetAddress();
        String kick = Constants.ZK_KICK;
        String ip = "10.1.10.65";
        zkUtil.registerEphemeral("/" + localIp + "/" + kick, ip);

    }

    @Test
    public void testAddRedis() {

        RedisNode node1 = new RedisNode("10.1.20.74", 6379, "ShineMoIpo");
        //RedisNode node2 = new RedisNode("127.0.0.1", 6380, "ShineMoIpo");

        RedisGroup group1 = new RedisGroup();
        group1.addRedisNode(node1);

        /*RedisGroup group2 = new RedisGroup();
        group2.addRedisNode(node2);*/

        List<RedisGroup> groupList = Lists.newArrayList(group1);

        zkUtil.registerPersist(PathEnum.REDIS_SERVER.getPathByIp(InetAddressUtil.getInetAddress()), Jsons.toJson(groupList));


    }

    @Test
    public void getRedisTest() {
        String value = zkUtil.get(PathEnum.REDIS_SERVER.getPathByIp(InetAddressUtil.getInetAddress()));
        List<RedisGroup> newGroupList = Jsons.fromJsonToList(value, RedisGroup[].class);
        for (RedisGroup group : newGroupList) {
            for (RedisNode node : group.getRedisNodeList()) {
                System.out.println(group + ToStringBuilder.reflectionToString(node, ToStringStyle.MULTI_LINE_STYLE));
            }

        }
    }


}
