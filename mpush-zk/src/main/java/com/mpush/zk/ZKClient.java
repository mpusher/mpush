package com.mpush.zk;

import com.mpush.log.Logs;
import com.mpush.tools.ConsoleLog;
import com.mpush.tools.Constants;
import com.mpush.tools.MPushUtil;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.tools.exception.ZKException;
import com.mpush.zk.listener.ZKDataChangeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZKClient {
    public static final ZKClient I = I();
    private ZKConfig zkConfig;
    private CuratorFramework client;
    private TreeCache cache;

    private synchronized static ZKClient I() {
        if (I == null) return new ZKClient();
        else return I;
    }

    private ZKClient() {
        try {
            init();
        } catch (Exception e) {
            throw new ZKException("init zk error, config=" + zkConfig, e);
        }
    }

    /**
     * 初始化
     */
    private void init() throws Exception {
        zkConfig = ZKConfig.build(ConfigCenter.I.zkIp())
                .setDigest(ConfigCenter.I.zkDigest())
                .setNamespace(ConfigCenter.I.zkNamespace());
        ConsoleLog.i("init zk client, config=" + zkConfig);
        Builder builder = CuratorFrameworkFactory
                .builder()
                .connectString(zkConfig.getHosts())
                .retryPolicy(new ExponentialBackoffRetry(zkConfig.getMinTime(), zkConfig.getMaxRetry(), zkConfig.getMaxTime()))
                .namespace(zkConfig.getNamespace());

        if (zkConfig.getConnectionTimeout() > 0) {
            builder.connectionTimeoutMs(zkConfig.getConnectionTimeout());
        }
        if (zkConfig.getSessionTimeout() > 0) {
            builder.sessionTimeoutMs(zkConfig.getSessionTimeout());
        }

        if (zkConfig.getDigest() != null) {
            builder.authorization("digest", zkConfig.getDigest().getBytes(Constants.UTF_8))
                    .aclProvider(new ACLProvider() {

                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        client = builder.build();
        client.start();
        ConsoleLog.i("init zk client waiting for connected...");
        if (!client.blockUntilConnected(1, TimeUnit.MINUTES)) {
            throw new ZKException("init zk error, config=" + zkConfig);
        }
        initLocalCache(zkConfig.getLocalCachePath());
        registerConnectionLostListener();
        Logs.ZK.info("zk client start success, server lists is:{}", zkConfig.getHosts());

        ConsoleLog.i("init zk client success...");
    }

    // 注册连接状态监听器
    private void registerConnectionLostListener() {
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            //TODO need close jvm?
            @Override
            public void stateChanged(final CuratorFramework client, final ConnectionState newState) {
                if (ConnectionState.LOST == newState) {
                    Logs.ZK.info("{} lost connection", MPushUtil.getInetAddress());
                } else if (ConnectionState.RECONNECTED == newState) {
                    Logs.ZK.info("{} reconnected", MPushUtil.getInetAddress());
                }
            }
        });
    }

    // 本地缓存
    private void initLocalCache(String cachePath) throws Exception {
        cache = new TreeCache(client, cachePath);
        cache.start();
    }

    private void waitClose() {
        try {
            Thread.sleep(600);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (null != cache) {
            cache.close();
        }
        waitClose();
        CloseableUtils.closeQuietly(client);
    }

    /**
     * 获取数据,先从本地获取，本地找不到，从远程获取
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        if (null == cache) {
            return null;
        }
        ChildData data = cache.getCurrentData(key);
        if (null != data) {
            return null == data.getData() ? null : new String(data.getData(), Constants.UTF_8);
        }
        return getFromRemote(key);
    }

    /**
     * 从远程获取数据
     *
     * @param key
     * @return
     */
    public String getFromRemote(final String key) {
        try {
            return new String(client.getData().forPath(key), Constants.UTF_8);
        } catch (final Exception ex) {
            Logs.ZK.error("getFromRemote:{}", key, ex);
            return null;
        }
    }

    /**
     * 获取子节点
     *
     * @param key
     * @return
     */
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {

                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
        } catch (final Exception ex) {
            Logs.ZK.error("getChildrenKeys:{}", key, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 判断路径是否存在
     *
     * @param key
     * @return
     */
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
            Logs.ZK.error("isExisted:{}", key, ex);
            return false;
        }
    }

    /**
     * 持久化数据
     *
     * @param key
     * @param value
     */
    public void registerPersist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes());
            } else {
                update(key, value);
            }
        } catch (final Exception ex) {
            Logs.ZK.error("persist:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 更新数据
     *
     * @param key
     * @param value
     */
    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Constants.UTF_8)).and().commit();
        } catch (final Exception ex) {
            Logs.ZK.error("update:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 注册临时数据
     *
     * @param key
     * @param value
     */
    public void registerEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Constants.UTF_8));
        } catch (final Exception ex) {
            Logs.ZK.error("persistEphemeral:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 注册临时顺序数据
     *
     * @param key
     */
    public void registerEphemeralSequential(final String key, final String value) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key, value.getBytes());
        } catch (final Exception ex) {
            Logs.ZK.error("persistEphemeralSequential:{},{}", key, value, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 注册临时顺序数据
     *
     * @param key
     */
    public void registerEphemeralSequential(final String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (final Exception ex) {
            Logs.ZK.error("persistEphemeralSequential:{}", key, ex);
            throw new ZKException(ex);
        }
    }

    /**
     * 删除数据
     *
     * @param key
     */
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (final Exception ex) {
            Logs.ZK.error("remove:{}", key, ex);
            throw new ZKException(ex);
        }
    }

    public void registerListener(ZKDataChangeListener listener) {
        cache.getListenable().addListener(listener);
    }

    public ZKConfig getZKConfig() {
        return zkConfig;
    }
}
