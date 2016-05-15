package com.mpush.tools;

import com.mpush.tools.config.ConfigCenter;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by ohun on 2015/12/25.
 *
 * @author ohun@live.cn
 */
public final class MPushUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MPushUtil.class);

    private static String LOCAL_IP;

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    private static String EXTRANET_IP;

    public static boolean isLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase("localhost")
                || host.equals("0.0.0.0")
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    public static String getLocalIp() {
        if (LOCAL_IP == null) {
            LOCAL_IP = getInetAddress();
        }
        return LOCAL_IP;
    }

    public static int getHeartbeat(int min, int max) {
        return Math.max(
                ConfigCenter.I.minHeartbeat(),
                Math.min(max, ConfigCenter.I.maxHeartbeat())
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
            Profiler.enter("start get inet addresss");
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
        } finally {
            Profiler.release();
        }
    }

    public static String getExtranetIp() {
        if (EXTRANET_IP == null) {
            EXTRANET_IP = getExtranetAddress();
        }
        return EXTRANET_IP;
    }

    public static String getExtranetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1 && !address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            LOGGER.warn("getExtranetAddress is null");
        } catch (Throwable e) {
            LOGGER.error("getExtranetAddress exception", e);
        }


        String localIp = getInetAddress();
        String remoteIp = null;
        Map<String, String> mapping = ConfigCenter.I.remoteIpMapping();
        if (mapping != null) {
            remoteIp = mapping.get(localIp);
        }

        if (remoteIp == null) {
            remoteIp = localIp;
        }

        return remoteIp;
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

    public static boolean telnet(String ip, int port) {
        TelnetClient client = new TelnetClient();
        try {
            client.connect(ip, port);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (IOException e) {
                LOGGER.error("disconnect error", e);
            }
        }
    }
}
