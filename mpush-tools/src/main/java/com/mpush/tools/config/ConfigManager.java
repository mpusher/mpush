package com.mpush.tools.config;

import com.mpush.tools.MPushUtil;

import static com.mpush.tools.MPushUtil.getInetAddress;

/**
 * Created by yxx on 2016/5/18.
 *
 * @author ohun@live.cn
 */
public class ConfigManager {
    public static final ConfigManager I = new ConfigManager();

    private ConfigManager() {
    }

    public int getHeartbeat(int min, int max) {
        return Math.max(
                CC.mp.core.min_heartbeat,
                Math.min(max, CC.mp.core.max_heartbeat)
        );
    }

    public String getLocalIp() {
        return MPushUtil.getLocalIp();
    }

    public String getPublicIp() {
        String localIp = getInetAddress();

        String remoteIp = CC.mp.net.public_ip_mapping.getString(localIp);

        return remoteIp;
    }
}
