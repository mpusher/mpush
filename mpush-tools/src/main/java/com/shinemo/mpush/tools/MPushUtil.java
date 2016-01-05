package com.shinemo.mpush.tools;

/**
 * Created by ohun on 2015/12/25.
 */
public final class MPushUtil {
    private static String LOCAL_IP;

    public static String getLocalIp() {
        if (LOCAL_IP == null) {
            LOCAL_IP = InetAddressUtil.getInetAddress();
        }
        return LOCAL_IP;
    }

    public static int getHeartbeat(int min, int max) {
        return Math.max(
                ConfigCenter.INSTANCE.getMinHeartbeat(),
                Math.min(max, ConfigCenter.INSTANCE.getMaxHeartbeat())
        );
    }

}
