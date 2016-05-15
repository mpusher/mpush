package com.mpush.tools.config;


import com.mpush.tools.redis.RedisGroup;
import com.mpush.tools.dns.DnsMapping;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;
import java.util.Map;

/**
 * 针对每个配置项，建议各个对象自己持有，不建议每次都通过ConfigCenter获取，有性能损耗
 */
@Sources({
        "classpath:config.properties",
        "file:/${user.dir}/config.properties"
})
public interface ConfigCenter extends Config {

    ConfigCenter I = ConfigFactory.create(ConfigCenter.class);

    /**
     * 最大包长度
     *
     * @return
     */
    @Key("max_packet_size")
    @DefaultValue("10240")
    int maxPacketSize();

    /**
     * 包启用压缩特性阈值
     *
     * @return
     */
    @Key("compress_limit")
    @DefaultValue("1024")
    int compressLimit();

    /**
     * 最小心跳间隔 10s
     *
     * @return
     */
    @Key("min_heartbeat")
    @DefaultValue("10000")
    int minHeartbeat();

    /**
     * 最大心跳间隔 10s
     *
     * @return
     */
    @Key("max_heartbeat")
    @DefaultValue("180000")
    int maxHeartbeat();

    /**
     * 最大心跳超时次数
     *
     * @return
     */
    @Key("max_hb_timeout_times")
    @DefaultValue("2")
    int maxHBTimeoutTimes();

    /**
     * 快速重连session超时时间
     *
     * @return
     */
    @Key("session_expired_time")
    @DefaultValue("86400")
    int sessionExpiredTime();

    /**
     * RSA密钥长度
     *
     * @return
     */
    @Key("ras_key_length")
    @DefaultValue("1024")
    int rsaKeyLength();

    /**
     * AES密钥长度
     *
     * @return
     */
    @Key("aes_key_length")
    @DefaultValue("16")
    int aesKeyLength();

    /**
     * 长连接服务端口
     *
     * @return
     */
    @Key("connection_server_port")
    @DefaultValue("3000")
    int connectionServerPort();

    /**
     * 网关服务端口
     *
     * @return
     */
    @Key("gateway_server_port")
    @DefaultValue("4000")
    int gatewayServerPort();


    /**
     * 控制台服务端口
     *
     * @return
     */
    @Key("admin_port")
    @DefaultValue("4001")
    int adminPort();

    /**
     * RSA私钥
     *
     * @return
     */
    @Key("private_key")
    @DefaultValue("MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=")
    String privateKey();

    /**
     * RSA公钥
     *
     * @return
     */
    @Key("public_key")
    @DefaultValue("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB")
    String publicKey();

    /**
     * redis集群机器列表格式ip:port:pwd,ip:port:pwd,ip:port:pwd
     * 多台机器用“,”分割
     *
     * @return
     */
    @Deprecated
    @Key("redis_ip")
    @DefaultValue("127.0.0.1:6379:ShineMoIpo")
    String redisIp();

    /**
     * zookeeper机器,格式ip:port
     *
     * @return
     */
    @Key("zk_ip")
    @DefaultValue("127.0.0.1:2181")
    String zkIp();

    /**
     * zookeeper 空间
     *
     * @return
     */
    @Key("zk_namespace")
    @DefaultValue("mpush")
    String zkNamespace();

    /**
     * zookeeper 权限密码
     *
     * @return
     */
    @Key("zk_digest")
    @DefaultValue("shinemoIpo")
    String zkDigest();

    /**
     * redis集群机器列表格式ip:port:pwd,ip:port:pwd,ip:port:pwd
     * 多台机器用“;”分割
     *
     * @return
     */
    @Separator(";")
    @Key("redis_group")
    @ConverterClass(RedisGroupConverter.class)
    List<RedisGroup> redisGroups();

    /**
     * 自动把配置的redis机器集群写入到zk
     *
     * @return
     */
    @Key("force_write_redis_group_info")
    boolean forceWriteRedisGroupInfo();

    @Key("scan_conn_task_cycle")
    @DefaultValue("59000")
    long scanConnTaskCycle();

    @Key("jvm_log_path")
    @DefaultValue("/opt/shinemo/mpush/")
    String logPath();

    @Key("http_proxy_enable")
    @DefaultValue("false")
    boolean httpProxyEnable();

    @Key("dns_mapping")
    @ConverterClass(DnsMappingConverter.class)
    Map<String, List<DnsMapping>> dnsMapping();

    @Key("max_http_client_conn_count_per_host")
    @DefaultValue("5")
    int maxHttpConnCountPerHost();

    //10s
    @Key("http_default_read_timeout")
    @DefaultValue("10000")
    int httpDefaultReadTimeout();

    @Key("online_and_offline_listener_ip")
    @DefaultValue("127.0.0.1")
    String onlineAndOfflineListenerIp();

    @Key("skip_dump")
    @DefaultValue("true")
    boolean skipDump();

    /**
     * 本机IP到外网Ip的映射 格式localIp:remoteIp,localIp:remoteIp
     *
     * @return
     */
    @Key("remote_ip_mapping")
    @ConverterClass(MapConverter.class)
    Map<String, String> remoteIpMapping();

}
