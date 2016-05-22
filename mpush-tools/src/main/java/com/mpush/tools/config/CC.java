package com.mpush.tools.config;

import com.mpush.tools.config.data.DnsMapping;
import com.mpush.tools.config.data.RedisGroup;
import com.mpush.tools.config.data.RedisServer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.typesafe.config.ConfigBeanFactory.create;
import static java.util.stream.Collectors.toCollection;

/**
 * Created by yxx on 2016/5/20.
 *
 * @author ohun@live.cn
 */
public interface CC {
    Config cfg = load();

    static Config load() {
        Config config = ConfigFactory.load();
        String custom_conf = "mp.conf";
        if (config.hasPath(custom_conf)) {
            File file = new File(config.getString(custom_conf));
            if (file.exists()) {
                Config custom = ConfigFactory.parseFile(file);
                config = custom.withFallback(config);
            }
        }
        return config;
    }

    interface mp {
        Config cfg = CC.cfg.getObject("mp").toConfig();

        String log_dir = cfg.getString("log.dir");
        String log_level = cfg.getString("log.level");

        interface security {
            Config cfg = mp.cfg.getObject("security").toConfig();

            int aes_key_length = cfg.getInt("aes-key-length");

            String public_key = cfg.getString("public-key");

            String private_key = cfg.getString("private-key");

            int ras_key_length = cfg.getInt("ras-key-length");

        }

        interface zk {
            Config cfg = mp.cfg.getObject("zk").toConfig();

            int sessionTimeoutMs = (int) cfg.getDuration("sessionTimeoutMs", TimeUnit.MILLISECONDS);

            String local_cache_path = cfg.getString("local-cache-path");

            int connectionTimeoutMs = (int) cfg.getDuration("connectionTimeoutMs", TimeUnit.MILLISECONDS);

            String namespace = cfg.getString("namespace");

            String digest = cfg.getString("digest");

            String server_address = cfg.getString("server-address");

            interface retry {
                Config cfg = zk.cfg.getObject("retry").toConfig();

                int maxRetries = cfg.getInt("maxRetries");

                int baseSleepTimeMs = (int) cfg.getDuration("baseSleepTimeMs", TimeUnit.MILLISECONDS);

                int maxSleepMs = (int) cfg.getDuration("maxSleepMs", TimeUnit.MILLISECONDS);

            }
        }

        interface thread {
            Config cfg = mp.cfg.getObject("thread").toConfig();

            interface pool {
                Config cfg = thread.cfg.getObject("pool").toConfig();

                interface boss {
                    Config cfg = pool.cfg.getObject("boss").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }

                interface work {
                    Config cfg = pool.cfg.getObject("work").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }

                interface event_bus {
                    Config cfg = pool.cfg.getObject("event-bus").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }

                interface http_proxy {
                    Config cfg = pool.cfg.getObject("http-proxy").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }

                interface biz {
                    Config cfg = pool.cfg.getObject("biz").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }

                interface mq {
                    Config cfg = pool.cfg.getObject("mq").toConfig();
                    int min = cfg.getInt("min");
                    int max = cfg.getInt("max");
                    int queue_size = cfg.getInt("queue-size");
                }
            }
        }

        interface redis {
            Config cfg = mp.cfg.getObject("redis").toConfig();
            boolean write_to_zk = cfg.getBoolean("write-to-zk");

            List<RedisGroup> cluster_group = cfg.getList("cluster-group")
                    .stream()
                    .map(v -> new RedisGroup(ConfigList.class.cast(v)
                                    .stream()
                                    .map(cv -> create(ConfigObject.class.cast(cv).toConfig(), RedisServer.class))
                                    .collect(toCollection(ArrayList::new))
                            )
                    )
                    .collect(toCollection(ArrayList::new));

            interface config {
                Config cfg = redis.cfg.getObject("config").toConfig();

                boolean jmxEnabled = cfg.getBoolean("jmxEnabled");

                int minIdle = cfg.getInt("minIdle");

                boolean testOnReturn = cfg.getBoolean("testOnReturn");

                long softMinEvictableIdleTimeMillis = cfg.getDuration("softMinEvictableIdleTimeMillis", TimeUnit.MILLISECONDS);

                boolean testOnBorrow = cfg.getBoolean("testOnBorrow");

                boolean testWhileIdle = cfg.getBoolean("testWhileIdle");

                long maxWaitMillis = cfg.getDuration("maxWaitMillis", TimeUnit.MILLISECONDS);

                String jmxNameBase = cfg.getString("jmxNameBase");

                long numTestsPerEvictionRun = cfg.getDuration("numTestsPerEvictionRun", TimeUnit.MILLISECONDS);

                String jmxNamePrefix = cfg.getString("jmxNamePrefix");

                long minEvictableIdleTimeMillis = cfg.getDuration("minEvictableIdleTimeMillis", TimeUnit.MILLISECONDS);

                boolean blockWhenExhausted = cfg.getBoolean("blockWhenExhausted");

                boolean fairness = cfg.getBoolean("fairness");

                long timeBetweenEvictionRunsMillis = cfg.getDuration("timeBetweenEvictionRunsMillis", TimeUnit.MILLISECONDS);

                boolean testOnCreate = cfg.getBoolean("testOnCreate");

                int maxIdle = cfg.getInt("maxIdle");

                boolean lifo = cfg.getBoolean("lifo");

                int maxTotal = cfg.getInt("maxTotal");

            }

        }

        interface monitor {
            Config cfg = mp.cfg.getObject("monitor").toConfig();

            boolean dump_stack = cfg.getBoolean("dump-stack");

        }

        interface http {
            Config cfg = mp.cfg.getObject("http").toConfig();

            boolean proxy_enable = cfg.getBoolean("proxy-enable");

            Map<String, List<DnsMapping>> dns_mapping = loadMapping();

            static Map<String, List<DnsMapping>> loadMapping() {
                Map<String, List<DnsMapping>> map = new HashMap<>();
                cfg.getObject("dns-mapping").forEach((s, v) ->
                        map.put(s, ConfigList.class.cast(v)
                                .stream()
                                .map(cv -> DnsMapping.parse((String) cv.unwrapped()))
                                .collect(toCollection(ArrayList::new))
                        )
                );
                return map;
            }

            int default_read_timeout = (int) cfg.getDuration("default-read-timeout", TimeUnit.MILLISECONDS);

            int max_conn_per_host = cfg.getInt("max-conn-per-host");

        }

        interface net {
            Config cfg = mp.cfg.getObject("net").toConfig();

            int gateway_server_port = cfg.getInt("gateway-server-port");

            int connect_server_port = cfg.getInt("connect-server-port");

            interface public_ip_mapping {
                Config cfg = net.cfg.getObject("public-host-mapping").toConfig();

                static String getString(String localIp) {
                    return cfg.hasPath(localIp) ? cfg.getString(localIp) : localIp;
                }
            }

            int admin_server_port = cfg.getInt("admin-server-port");

        }

        interface core {
            Config cfg = mp.cfg.getObject("core").toConfig();

            int session_expired_time = (int) cfg.getDuration("session-expired-time").getSeconds();

            int max_heartbeat = (int) cfg.getDuration("max-heartbeat", TimeUnit.MILLISECONDS);

            int max_packet_size = (int) cfg.getMemorySize("max-packet-size").toBytes();

            int min_heartbeat = (int) cfg.getDuration("min-heartbeat", TimeUnit.MILLISECONDS);

            long compress_threshold = cfg.getBytes("compress-threshold");

            int max_hb_timeout_times = cfg.getInt("max-hb-timeout-times");
        }
    }

}