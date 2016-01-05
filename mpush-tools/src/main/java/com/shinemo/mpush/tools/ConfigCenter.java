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
    private int rasKeyLength = 1024;
    private int aesKeyLength = 16;
    private int connectionServerPort = 3000;
    private int gatewayServerPort = 4000;
    private String privateKey;
    private String publicKey;

    public void init() throws IOException {
        cfg.load(this.getClass().getResourceAsStream("/config.properties"));
        connectionServerPort = getInt("CONNECTION_SERVER_PORT", connectionServerPort);
        gatewayServerPort = getInt("GATEWAY_SERVER_PORT", gatewayServerPort);
        privateKey = getString("PRIVATE_KEY", privateKey);
        publicKey = getString("PUBLIC_KEY", privateKey);
        maxPacketSize = getInt("MAX_PACKET_SIZE", maxPacketSize);
        compressLimit = getInt("COMPRESS_LIMIT", compressLimit);
        minHeartbeat = getInt("MIN_HEARTBEAT", minHeartbeat);
        maxHeartbeat = getInt("MAX_HEARTBEAT", maxHeartbeat);
        maxHBTimeoutTimes = getInt("MAX_HB_TIMEOUT_TIMES", maxHBTimeoutTimes);
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
}
