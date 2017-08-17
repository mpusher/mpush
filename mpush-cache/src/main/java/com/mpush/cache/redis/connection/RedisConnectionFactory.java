/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mpush.cache.redis.connection;

import com.mpush.cache.redis.RedisServer;
import com.mpush.tools.config.data.RedisNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Connection factory creating <a href="http://github.com/xetorthio/jedis">Jedis</a> based connections.
 *
 * @author Costin Leau
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class RedisConnectionFactory {

    private final static Logger log = LoggerFactory.getLogger(RedisConnectionFactory.class);

    private String hostName = "localhost";
    private int port = Protocol.DEFAULT_PORT;
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    private String password;

    private String sentinelMaster;
    private List<RedisNode> redisServers;
    private boolean isCluster = false;
    private int dbIndex = 0;

    private JedisShardInfo shardInfo;
    private Pool<Jedis> pool;
    private JedisCluster cluster;
    private JedisPoolConfig poolConfig = new JedisPoolConfig();

    /**
     * Constructs a new <code>JedisConnectionFactory</code> instance with default settings (default connection pooling, no
     * shard information).
     */
    public RedisConnectionFactory() {
    }

    /**
     * Returns a Jedis instance to be used as a Redis connection. The instance can be newly created or retrieved from a
     * pool.
     */
    protected Jedis fetchJedisConnector() {
        try {

            if (pool != null) {
                return pool.getResource();
            }
            Jedis jedis = new Jedis(getShardInfo());
            // force initialization (see Jedis issue #82)
            jedis.connect();
            return jedis;
        } catch (Exception ex) {
            throw new RuntimeException("Cannot get Jedis connection", ex);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void init() {
        if (shardInfo == null) {
            shardInfo = new JedisShardInfo(hostName, port);

            if (StringUtils.isNotEmpty(password)) {
                shardInfo.setPassword(password);
            }

            if (timeout > 0) {
                shardInfo.setConnectionTimeout(timeout);
            }
        }

        if (isCluster) {
            this.cluster = createCluster();
        } else {
            this.pool = createPool();
        }
    }

    private Pool<Jedis> createPool() {
        if (StringUtils.isNotBlank(sentinelMaster)) {
            return createRedisSentinelPool();
        }
        return createRedisPool();
    }

    /**
     * Creates {@link JedisSentinelPool}.
     *
     * @return
     * @since 1.4
     */
    protected Pool<Jedis> createRedisSentinelPool() {
        Set<String> hostAndPorts = redisServers
                .stream()
                .map(redisNode -> new HostAndPort(redisNode.host, redisNode.port).toString())
                .collect(Collectors.toSet());

        return new JedisSentinelPool(sentinelMaster, hostAndPorts, poolConfig, getShardInfo().getSoTimeout(), getShardInfo().getPassword());
    }


    /**
     * Creates {@link JedisPool}.
     *
     * @return
     * @since 1.4
     */
    protected Pool<Jedis> createRedisPool() {
        return new JedisPool(getPoolConfig(), shardInfo.getHost(), shardInfo.getPort(), shardInfo.getSoTimeout(), shardInfo.getPassword());
    }

    /**
     * @return
     * @since 1.7
     */
    protected JedisCluster createCluster() {

        Set<HostAndPort> hostAndPorts = redisServers
                .stream()
                .map(redisNode -> new HostAndPort(redisNode.host, redisNode.port))
                .collect(Collectors.toSet());


        if (StringUtils.isNotEmpty(getPassword())) {
            throw new IllegalArgumentException("Jedis does not support password protected Redis Cluster configurations!");
        }
        int redirects = 5;
        return new JedisCluster(hostAndPorts, timeout, redirects, poolConfig);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() {
        if (pool != null) {
            try {
                pool.destroy();
            } catch (Exception ex) {
                log.warn("Cannot properly close Jedis pool", ex);
            }
            pool = null;
        }
        if (cluster != null) {
            try {
                cluster.close();
            } catch (Exception ex) {
                log.warn("Cannot properly close Jedis cluster", ex);
            }
            cluster = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConnectionFactory#getConnection()
     */
    public Jedis getJedisConnection() {
        Jedis jedis = fetchJedisConnector();
        if (dbIndex > 0 && jedis != null) {
            jedis.select(dbIndex);
        }
        return jedis;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.connection.RedisConnectionFactory#getClusterConnection()
     */
    public JedisCluster getClusterConnection() {
        return cluster;
    }

    public boolean isCluster() {
        return isCluster;
    }

    /**
     * Returns the Redis hostName.
     *
     * @return Returns the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the Redis hostName.
     *
     * @param hostName The hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the password used for authenticating with the Redis server.
     *
     * @return password for authentication
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password used for authenticating with the Redis server.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the port used to connect to the Redis instance.
     *
     * @return Redis port.
     */
    public int getPort() {
        return port;

    }

    /**
     * Sets the port used to connect to the Redis instance.
     *
     * @param port Redis port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the shardInfo.
     *
     * @return Returns the shardInfo
     */
    public JedisShardInfo getShardInfo() {
        return shardInfo;
    }

    /**
     * Sets the shard info for this factory.
     *
     * @param shardInfo The shardInfo to set.
     */
    public void setShardInfo(JedisShardInfo shardInfo) {
        this.shardInfo = shardInfo;
    }

    /**
     * Returns the timeout.
     *
     * @return Returns the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the poolConfig.
     *
     * @return Returns the poolConfig
     */
    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    /**
     * Sets the pool configuration for this factory.
     *
     * @param poolConfig The poolConfig to set.
     */
    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    /**
     * Returns the index of the database.
     *
     * @return Returns the database index
     */
    public int getDatabase() {
        return dbIndex;
    }

    /**
     * Sets the index of the database used by this connection factory. Default is 0.
     *
     * @param index database index
     */
    public void setDatabase(int index) {
        this.dbIndex = index;
    }

    public void setCluster(boolean cluster) {
        isCluster = cluster;
    }

    public void setSentinelMaster(String sentinelMaster) {
        this.sentinelMaster = sentinelMaster;
    }

    public void setRedisServers(List<RedisNode> redisServers) {
        if (redisServers == null || redisServers.isEmpty()) {
            throw new IllegalArgumentException("redis server node can not be empty, please check your conf.");
        }
        this.redisServers = redisServers;
        this.hostName = redisServers.get(0).getHost();
        this.port = redisServers.get(0).getPort();
    }
}
