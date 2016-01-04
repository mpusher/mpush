package com.shinemo.mpush.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ohun on 2015/12/25.
 */
public final class MPushUtil {
    private static String LOCAL_IP;

    public static String getLocalIp() {
        if (LOCAL_IP == null) {
            try {
                LOCAL_IP = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                LOCAL_IP = "127.0.0.1";
            }
        }
        return LOCAL_IP;
    }

    public static int getHeartbeat(int min, int max) {
        return Constants.HEARTBEAT_TIME;
    }

}
