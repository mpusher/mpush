package com.shinemo.mpush.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shinemo.mpush.tools.config.ConfigCenter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
    
    public static String getExtranetAddress() throws Exception{
    	 try {
             Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
             InetAddress address = null;
             while (interfaces.hasMoreElements()) {
                 NetworkInterface ni = interfaces.nextElement();
                 Enumeration<InetAddress> addresses = ni.getInetAddresses();
                 while (addresses.hasMoreElements()) {
                     address = addresses.nextElement();
                     if(!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1 && !address.isSiteLocalAddress()){
                    	 return address.getHostAddress();
                     }
                 }
             }
             LOGGER.warn("getExtranetAddress is null");
             return null;
         } catch (Throwable e) {
             LOGGER.error("getExtranetAddress exception", e);
             throw new Exception(e);
         }
    }

    public static String headerToString(Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            StringBuilder sb = new StringBuilder(headers.size() * 64);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey())
                        .append(':')
                        .append(entry.getValue()).append('\n');
            }
            return sb.toString();
        }
        return null;
    }


    public static Map<String, String> headerFromString(String headersString) {
        if (headersString == null) return null;
        Map<String, String> headers = new HashMap<>();
        int L = headersString.length();
        String name, value = null;
        for (int i = 0, start = 0; i < L; i++) {
            char c = headersString.charAt(i);
            if (c != '\n') continue;
            if (start >= L - 1) break;
            String header = headersString.substring(start, i);
            start = i + 1;
            int index = header.indexOf(':');
            if (index <= 0) continue;
            name = header.substring(0, index);
            if (index < header.length() - 1) {
                value = header.substring(index + 1);
            }
            headers.put(name, value);
        }
        return headers;
    }
}
