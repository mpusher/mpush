package com.shinemo.mpush.tools;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by ohun on 2016/1/5.
 */
public final class ConfigCenter {
    public static final ConfigCenter INSTANCE = new ConfigCenter();
    private transient Properties cfg = new Properties();
    private int maxPacketSize = 10240;//10k
    private int compressLimit = 10240;//10k
    private int minHeartbeat = 1000 * 10;//10s
    private int maxHeartbeat = 1000 * 60 * 30;//30min
    private int maxHBTimeoutTimes = 2;
    private int sessionExpiredTime = 86400;//unit second
    private int rasKeyLength = 1024;
    private int aesKeyLength = 16;
    private int connectionServerPort = 3000;
    private int gatewayServerPort = 4000;
    private String privateKey = "MIIBNgIBADANBgkqhkiG9w0BAQEFAASCASAwggEcAgEAAoGBAKCE8JYKhsbydMPbiO7BJVq1pbuJWJHFxOR7L8Hv3ZVkSG4eNC8DdwAmDHYu/wadfw0ihKFm2gKDcLHp5yz5UQ8PZ8FyDYvgkrvGV0ak4nc40QDJWws621dm01e/INlGKOIStAAsxOityCLv0zm5Vf3+My/YaBvZcB5mGUsPbx8fAgEAAoGAAy0+WanRqwRHXUzt89OsupPXuNNqBlCEqgTqGAt4Nimq6Ur9u2R1KXKXUotxjp71Ubw6JbuUWvJg+5Rmd9RjT0HOUEQF3rvzEepKtaraPhV5ejEIrB+nJWNfGye4yzLdfEXJBGUQzrG+wNe13izfRNXI4dN/6Q5npzqaqv0E1CkCAQACAQACAQACAQACAQA=";
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    private String zkNamespace = "mpush";
    private String zkServer = "127.0.0.1:2181";
    private String redisServer = "127.0.0.1:6379:ShineMoIpo";

    public void init() throws IOException {
        cfg.load(this.getClass().getResourceAsStream("/config.properties"));
        privateKey = getString("PRIVATE_KEY", privateKey);
        publicKey = getString("PUBLIC_KEY", privateKey);
        zkNamespace = getString("ZK_NAMESPACE", zkNamespace);
        zkServer = getString("ZK_SERVER", zkServer);
        redisServer = getString("REDIS_SERVER", redisServer);

        gatewayServerPort = getInt("GATEWAY_SERVER_PORT", gatewayServerPort);
        connectionServerPort = getInt("CONNECTION_SERVER_PORT", connectionServerPort);
        maxPacketSize = getInt("MAX_PACKET_SIZE", maxPacketSize);
        compressLimit = getInt("COMPRESS_LIMIT", compressLimit);
        minHeartbeat = getInt("MIN_HEARTBEAT", minHeartbeat);
        maxHeartbeat = getInt("MAX_HEARTBEAT", maxHeartbeat);
        maxHBTimeoutTimes = getInt("MAX_HB_TIMEOUT_TIMES", maxHBTimeoutTimes);
        sessionExpiredTime = getInt("SESSION_EXPIRED_TIME", sessionExpiredTime);
        rasKeyLength = getInt("RAS_KEY_LENGTH", rasKeyLength);
        aesKeyLength = getInt("AES_KEY_LENGTH", aesKeyLength);
        maxPacketSize = getInt("MAX_PACKET_SIZE", maxPacketSize);
    }

    public String getString(String key, String defaultValue) {
        return cfg.getProperty(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = cfg.getProperty(key);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = cfg.getProperty(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public long getLong(String key, long defaultValue) {
        String value = cfg.getProperty(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public int getCompressLimit() {
        return compressLimit;
    }

    public int getMinHeartbeat() {
        return minHeartbeat;
    }

    public int getMaxHeartbeat() {
        return maxHeartbeat;
    }

    public int getMaxHBTimeoutTimes() {
        return maxHBTimeoutTimes;
    }

    public int getSessionExpiredTime() {
        return sessionExpiredTime;
    }

    public int getRasKeyLength() {
        return rasKeyLength;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public int getConnectionServerPort() {
        return connectionServerPort;
    }

    public int getGatewayServerPort() {
        return gatewayServerPort;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getRedisServer() {
        return redisServer;
    }

    public String getZkServer() {
        return zkServer;
    }

    public String getZkNamespace() {
        return zkNamespace;
    }
}
