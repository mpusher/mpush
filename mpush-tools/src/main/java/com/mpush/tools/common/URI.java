/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.mpush.tools.common;

import com.mpush.api.Constants;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by ohun on 2016/12/20.
 *
 * @author ohun@live.cn (夜色)
 */
public final class URI {

    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final int port;
    private final String path;
    private final Map<String, String> parameters;

    private volatile transient Map<String, Number> numbers;
    private volatile transient Map<String, java.net.URI> urls;
    private volatile transient String ip;
    private volatile transient String full;
    private volatile transient String identity;
    private volatile transient String parameter;
    private volatile transient String string;

    protected URI() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URI(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, (Map<String, String>) null);
    }

    public URI(String protocol, String host, int port, String[] pairs) { // 变长参数...与下面的path参数冲突，改为数组
        this(protocol, null, null, host, port, null, toStringMap(pairs));
    }

    public URI(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URI(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    public URI(String protocol, String host, int port, String path, String... pairs) {
        this(protocol, null, null, host, port, path, toStringMap(pairs));
    }

    public URI(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URI(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

    public URI(String protocol, String username, String password, String host, int port, String path, String... pairs) {
        this(protocol, username, password, host, port, path, toStringMap(pairs));
    }

    public URI(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if ((username == null || username.length() == 0) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }


    public static URI valueOf(java.net.URI uri) {
        String[] params = uri.getQuery() == null ? null : uri.getQuery().split("&");
        return new URI(uri.getScheme(), uri.getHost(), uri.getPort(), params);
    }


    /**
     * Parse url string
     *
     * @param url URL string
     * @return URL instance
     * @see URI
     */
    public static URI valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf('?');
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) host = url;
        return new URI(protocol, username, password, host, port, path, parameters);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthority() {
        if ((username == null || username.length() == 0) && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) + ":" + (password == null ? "" : password);
    }

    public String getHost() {
        return host;
    }


    public String getIp() {
        if (ip == null) {
            try {
                ip = InetAddress.getByName(host).getHostAddress();
            } catch (UnknownHostException e) {
                ip = host;
            }
        }
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }


    public String getPath() {
        return path;
    }

    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public URI setProtocol(String protocol) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setUsername(String username) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setPassword(String password) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setHost(String host) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setPort(int port) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public URI setPath(String path) {
        return new URI(protocol, username, password, host, port, path, getParameters());
    }

    public Map<String, String> getParameters() {
        return parameters;
    }


    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.split(",");
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) { // 允许并发重复创建
            numbers = new ConcurrentHashMap<String, Number>();
        }
        return numbers;
    }


    public double getParameter(String key, double defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.doubleValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        getNumbers().put(key, d);
        return d;
    }

    public float getParameter(String key, float defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(key, f);
        return f;
    }

    public long getParameter(String key, long defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    public short getParameter(String key, short defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.shortValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(key, s);
        return s;
    }

    public byte getParameter(String key, byte defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.byteValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(key, b);
        return b;
    }


    public char getParameter(String key, char defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    public String getMethodParameter(String method, String key) {
        String value = parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return getParameter(key);
        }
        return value;
    }

    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public double getMethodParameter(String method, String key, double defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double d = Double.parseDouble(value);
        getNumbers().put(methodKey, d);
        return d;
    }

    public float getMethodParameter(String method, String key, float defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(methodKey, f);
        return f;
    }

    public long getMethodParameter(String method, String key, long defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(methodKey, l);
        return l;
    }

    public int getMethodParameter(String method, String key, int defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(methodKey, i);
        return i;
    }

    public short getMethodParameter(String method, String key, short defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.shortValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        short s = Short.parseShort(value);
        getNumbers().put(methodKey, s);
        return s;
    }

    public byte getMethodParameter(String method, String key, byte defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.byteValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        byte b = Byte.parseByte(value);
        getNumbers().put(methodKey, b);
        return b;
    }

    public double getMethodPositiveParameter(String method, String key, double defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        double value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public float getMethodPositiveParameter(String method, String key, float defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        float value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public long getMethodPositiveParameter(String method, String key, long defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        long value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public int getMethodPositiveParameter(String method, String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public short getMethodPositiveParameter(String method, String key, short defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        short value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public byte getMethodPositiveParameter(String method, String key, byte defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        byte value = getMethodParameter(method, key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    public char getMethodParameter(String method, String key, char defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public boolean getMethodParameter(String method, String key, boolean defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    //$NON-NLS-分组参数结束$

    public boolean isLocalHost() {
        return host != null
                && (Pattern.compile("127(\\.\\d{1,3}){3}$").matcher(host).matches()
                || host.equalsIgnoreCase("localhost"));
    }

    public boolean isAnyHost() {
        return Constants.ANY_HOST.equals(host);
    }

    public URI addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, Enum<?> value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, Number value) {
        if (value == null) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) return this;
        return addParameter(key, String.valueOf(value));
    }

    public URI addParameter(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        // 如果没有修改，直接返回。
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URI(protocol, username, password, host, port, path, map);
    }

    public URI addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0 || value == null || value.length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URI(protocol, username, password, host, port, path, map);
    }

    /**
     * Add parameters to a new url.
     *
     * @param parameters parameters
     * @return A new URL
     */
    public URI addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null && entry.getValue() != null || !value.equals(entry.getValue())) {
                hasAndEqual = false;
                break;
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual) return this;

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new URI(protocol, username, password, host, port, path, map);
    }

    public URI addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new URI(protocol, username, password, host, port, path, map);
    }

    public URI addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }

    public URI removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    public URI removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    public URI removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new URI(protocol, username, password, host, port, path, map);
    }

    public URI clearParameters() {
        return new URI(protocol, username, password, host, port, path, new HashMap<String, String>());
    }

    public String getRawParameter(String key) {
        if ("protocol".equals(key)) {
            return protocol;
        }
        if ("username".equals(key)) {
            return username;
        }
        if ("password".equals(key)) {
            return password;
        }
        if ("host".equals(key)) {
            return host;
        }
        if ("port".equals(key)) {
            return String.valueOf(port);
        }
        if ("path".equals(key)) {
            return path;
        }
        return getParameter(key);
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>(parameters);
        if (protocol != null) {
            map.put("protocol", protocol);
        }
        if (username != null) {
            map.put("username", username);
        }
        if (password != null) {
            map.put("password", password);
        }
        if (host != null) {
            map.put("host", host);
        }
        if (port > 0) {
            map.put("port", String.valueOf(port));
        }
        if (path != null) {
            map.put("path", path);
        }
        return map;
    }

    public String toString() {
        if (string != null) {
            return string;
        }
        return string = buildString(false, true); // no show username and password
    }

    public String toString(String... parameters) {
        return buildString(false, true, parameters); // no show username and password
    }

    public String toIdentityString() {
        if (identity != null) {
            return identity;
        }
        return identity = buildString(true, false); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toIdentityString(String... parameters) {
        return buildString(true, false, parameters); // only return identity message, see the method "equals" and "hashCode"
    }

    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    public String toFullString(String... parameters) {
        return buildString(true, true, parameters);
    }

    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0 && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path = getPath();
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public java.net.URI toJavaURI() {
        return java.net.URI.create(toString());
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public static Map<String, String> toStringMap(String... pairs) {
        Map<String, String> parameters = new HashMap<String, String>();
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i = i + 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URI uri = (URI) o;

        if (port != uri.port) return false;
        if (protocol != null ? !protocol.equals(uri.protocol) : uri.protocol != null) return false;
        if (username != null ? !username.equals(uri.username) : uri.username != null) return false;
        if (password != null ? !password.equals(uri.password) : uri.password != null) return false;
        if (host != null ? !host.equals(uri.host) : uri.host != null) return false;
        if (path != null ? !path.equals(uri.path) : uri.path != null) return false;
        if (parameters != null ? !parameters.equals(uri.parameters) : uri.parameters != null) return false;
        return true;
    }
}
