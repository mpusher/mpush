/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.tools;

import com.alibaba.fastjson.JSON;
import com.mpush.api.Constants;
import com.mpush.tools.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoxu.yxx on 15/8/7.
 *
 * @author ohun@live.cn (夜色)
 */
public final class Jsons {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jsons.class);

    public static String toJson(Object bean) {
        try {
            return JSON.toJSONString(bean);
        } catch (Exception e) {
            LOGGER.error("Jsons.toJson ex, bean=" + bean, e);
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {

        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            LOGGER.error("Jsons.fromJson ex, json=" + json + ", clazz=" + clazz, e);
        }
        return null;
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        return fromJson(new String(json, Constants.UTF_8), clazz);
    }

    public static <T> List<T> fromJsonToList(String json, Class<T> type) {
        try {
            return JSON.parseArray(json, type);
        } catch (Exception e) {
            LOGGER.error("Jsons.fromJsonToList ex, json=" + json + ", type=" + type, e);
        }
        return null;
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return JSON.parseObject(json, type);
        } catch (Exception e) {
            LOGGER.error("Jsons.fromJson ex, json=" + json + ", type=" + type, e);
        }
        return null;
    }

    public static boolean mayJson(String json) {
        if (Strings.isBlank(json))
            return false;
        if (json.charAt(0) == '{' && json.charAt(json.length() - 1) == '}')
            return true;
        if (json.charAt(0) == '[' && json.charAt(json.length() - 1) == ']')
            return true;
        return false;
    }

    public static String toJson(Map<String, String> map) {
        if (map == null || map.isEmpty())
            return "{}";
        StringBuilder sb = new StringBuilder(64 * map.size());
        sb.append('{');
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        if (it.hasNext()) {
            append(it.next(), sb);
        }
        while (it.hasNext()) {
            sb.append(',');
            append(it.next(), sb);
        }
        sb.append('}');
        return sb.toString();
    }

    private static void append(Map.Entry<String, String> entry, StringBuilder sb) {
        String key = entry.getKey(), value = entry.getValue();
        if (value == null)
            value = Strings.EMPTY;
        sb.append('"').append(key).append('"');
        sb.append(':');
        sb.append('"').append(value).append('"');
    }
}
