package com.shinemo.mpush.tools.zk;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;

import com.shinemo.mpush.tools.Constants;

public class ZkUtil {

	private static final Logger log = LoggerFactory.getLogger(ZkUtil.class);
	
	private static final ZkConfig config = new ZkConfig(Constants.ZK_IPS, Constants.ZK_NAME_SPACE);

	public static final ZkUtil instance = new ZkUtil(config);

	static {
		instance.init();
	}
	
	private ZkConfig zkConfig;
	private CuratorFramework client;
    private TreeCache cache;

	private ZkUtil(ZkConfig zkConfig) {
		this.zkConfig = zkConfig;
	}
	
	public ZkConfig getZkConfig() {
		return zkConfig;
	}

	public CuratorFramework getClient() {
		return client;
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		log.warn("start registry zk, server lists is: {}.", zkConfig.getIpLists());
		Builder builder = CuratorFrameworkFactory.builder().connectString(zkConfig.getIpLists())
				.retryPolicy(new ExponentialBackoffRetry(zkConfig.getMinTime(), zkConfig.getMaxRetry(), zkConfig.getMaxTime())).namespace(zkConfig.getNamespace());
		if (zkConfig.getConnectionTimeout() > 0) {
			builder.connectionTimeoutMs(zkConfig.getConnectionTimeout());
		}
		if (zkConfig.getSessionTimeout() > 0) {
			builder.sessionTimeoutMs(zkConfig.getSessionTimeout());
		}
		if (StringUtils.isNoneBlank(zkConfig.getDigest())) {
			builder.authorization("digest", zkConfig.getDigest().getBytes(Charset.forName("UTF-8"))).aclProvider(new ACLProvider() {

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
		try {
			client.blockUntilConnected();
			cacheData();
		} catch (final Exception ex) {
			log.error("zk connection error" + ToStringBuilder.reflectionToString(zkConfig, ToStringStyle.DEFAULT_STYLE));
		}

	}
	
	//本地缓存
    public void cacheData() throws Exception {
        cache = new TreeCache(client, zkConfig.getLocalCachePath());
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
	 * @param key
	 * @return
	 */
    public String get(final String key) {
        if (null == cache) {
            return null;
        }
        ChildData resultIncache = cache.getCurrentData(key);
        if (null != resultIncache) {
            return null == resultIncache.getData() ? null : new String(resultIncache.getData(), Charset.forName("UTF-8"));
        }
        return getFromRemote(key);
    }
    
    /**
     * 从远程获取数据
     * @param key
     * @return
     */
    public String getFromRemote(final String key) {
        try {
            return new String(client.getData().forPath(key), Charset.forName("UTF-8"));
        } catch (final Exception ex) {
        	log.error("getDirectly" + ToStringBuilder.reflectionToString(key, ToStringStyle.DEFAULT_STYLE),ex);
            return null;
        }
    }
    
    /**
     * 获取子节点
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
            log.error("getChildrenKeys" + ToStringBuilder.reflectionToString(key, ToStringStyle.DEFAULT_STYLE),ex);
            return Collections.emptyList();
        }
    }
    
    /**
     * 判断路径是否存在
     * @param key
     * @return
     */
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
        } catch (final Exception ex) {
        	log.error("isExisted" + ToStringBuilder.reflectionToString(key, ToStringStyle.DEFAULT_STYLE),ex);
            return false;
        }
    }
    
    /**
     * 持久化数据
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
        	log.error("persist" + key+","+value,ex);
        }
    }
    
    /**
     * 更新数据
     * @param key
     * @param value
     */
    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charset.forName("UTF-8"))).and().commit();
        } catch (final Exception ex) {
        	log.error("update" + key+","+value,ex);
        }
    }
    
    /**
     * 注册临时数据
     * @param key
     * @param value
     */
    public void registerEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Charset.forName("UTF-8")));
        } catch (final Exception ex) {
        	log.error("persistEphemeral" + key+","+value,ex);
        }
    }
    
    /**
     * 注册临时顺序数据
     * @param key
     */
    public void registerEphemeralSequential(final String key,final String value) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key, value.getBytes());
        } catch (final Exception ex) {
        	log.error("persistEphemeralSequential" + key,ex);
        }
    }
    
    /**
     * 注册临时顺序数据
     * @param key
     */
    public void registerEphemeralSequential(final String key) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key);
        } catch (final Exception ex) {
        	log.error("persistEphemeralSequential" + key,ex);
        }
    }
    
    /**
     * 删除数据
     * @param key
     */
    public void remove(final String key) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(key);
        } catch (final Exception ex) {
        	log.error("remove" + key,ex);
        }
    }
    
    public TreeCache getCache() {
        return cache;
    }

}
