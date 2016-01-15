package com.shinemo.mpush.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.config.ConfigCenter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by ohun on 2015/12/25.
 */
public final class MPushUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MPushUtil.class);

    private static String LOCAL_IP;

    public static String getLocalIp() {
        if (LOCAL_IP == null) {
            LOCAL_IP = getInetAddress();
        }
        return LOCAL_IP;
    }

    public static int getHeartbeat(int min, int max) {
        return Math.max(
        		ConfigCenter.holder.minHeartbeat(),
                Math.min(max, ConfigCenter.holder.maxHeartbeat())
        );
    }

    /**
     * 获取本机ip
     * 只获取第一块网卡绑定的ip地址
     *
     * @return
     */
    public static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1 && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            LOGGER.warn("getInetAddress is null");
            return "127.0.0.1";
        } catch (Throwable e) {
            LOGGER.error("getInetAddress exception", e);
            return "127.0.0.1";
        }
    }
}
