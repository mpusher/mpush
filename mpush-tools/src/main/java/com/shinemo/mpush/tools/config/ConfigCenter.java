package com.shinemo.mpush.tools.config;


import com.shinemo.mpush.tools.redis.RedisGroup;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;

import java.util.List;

/**
 * 针对每个配置项，建议各个对象自己持有，不建议每次都通过ConfigCenter获取，有性能损耗
 */
@Sources({
        "classpath:config.properties",
        "file:/${user.dir}/config.properties"
})
public interface ConfigCenter extends Config {

    ConfigCenter holder = ConfigFactory.create(ConfigCenter.class);

    @Key("max_packet_size")
    @DefaultValue("10240")
    int maxPacketSize();

    @Key("compress_limit")
    @DefaultValue("10240")
    int compressLimit();

    @Key("min_heartbeat")
    @DefaultValue("10000")
    int minHeartbeat();

    @Key("max_heartbeat")
    @DefaultValue("180000") //180秒
    int maxHeartbeat();

    @Key("max_hb_timeout_times")
    @DefaultValue("2")
    int maxHBTimeoutTimes();

    @Key("session_expired_time")
    @DefaultValue("86400")
    int sessionExpiredTime();

    @Key("ras_key_length")
    @DefaultValue("1024")
    int rasKeyLength();

    @Key("aes_key_length")
    @DefaultValue("16")
    int aesKeyLength();

    @Key("connection_server_port")
    @DefaultValue("3000")
    int connectionServerPort();

    @Key("gateway_server_port")
    @DefaultValue("4000")
    int gatewayServerPort();

    @Key("private_key")
    @DefaultValue("MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=")
    String privateKey();

    @Key("public_key")
    @DefaultValue("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB")
    String publicKey();

    @Key("redis_ip")
    @DefaultValue("127.0.0.1:6379:ShineMoIpo")
    String redisIp();

    @Key("zk_ip")
    @DefaultValue("127.0.0.1:2181")
    String zkIp();

    @Key("zk_namespace")
    @DefaultValue("mpush")
    String zkNamespace();

    @Key("zk_digest")
    @DefaultValue("shinemoIpo")
    String zkDigest();

    @Separator(";")
    @Key("redis_group")
    @ConverterClass(RedisGroupConverter.class)
    List<RedisGroup> redisGroups();
    
    @Key("force_write_redis_group_info")
    boolean forceWriteRedisGroupInfo();

    @Key("scan_conn_task_cycle")
    @DefaultValue("59000")
    long scanConnTaskCycle();

    @Key("jvm_log_path")
    @DefaultValue("/opt/shinemo/mpush/")
    String logPath();
    
    
}
