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

package com.mpush.test.spi;

import com.mpush.api.Constants;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.tools.Jsons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ohun on 2016/12/28.
 *
 * @author ohun@live.cn (夜色)
 */
@SuppressWarnings("unchecked")
public final class FileCacheManger implements CacheManager {
    public static final FileCacheManger I = new FileCacheManger();
    private Map<String, Object> map = new HashMap<>();

    @Override
    public void init() {
        loadFormFile();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void del(String key) {
        map.remove(key);
        writeToFile();
    }

    @Override
    public long hincrBy(String key, String field, long value) {
        Map fields = ((Map) map.computeIfAbsent(key, k -> new ConcurrentHashMap<>()));
        Number num = (Number) fields.get(field);
        long result = num.longValue() + 1;
        fields.put(field, result);
        writeToFile();
        return result;
    }

    @Override
    public void set(String key, String value) {
        map.put(key, value);
        writeToFile();
    }

    @Override
    public void set(String key, String value, int expireTime) {
        map.put(key, value);
        writeToFile();
    }

    @Override
    public void set(String key, Object value, int expireTime) {
        map.put(key, value);
        writeToFile();
    }

    @Override
    public <T> T get(String key, Class<T> tClass) {
        Object obj = map.get(key);
        if (obj == null) return null;
        return Jsons.fromJson(obj.toString(), tClass);
    }

    @Override
    public void hset(String key, String field, String value) {
        ((Map) map.computeIfAbsent(key, k -> new ConcurrentHashMap<>())).put(field, value);
        writeToFile();
    }

    @Override
    public void hset(String key, String field, Object value) {
        ((Map) map.computeIfAbsent(key, k -> new ConcurrentHashMap<>())).put(field, value);
        writeToFile();
    }

    @Override
    public <T> T hget(String key, String field, Class<T> tClass) {
        Object obj = ((Map) map.computeIfAbsent(key, k -> new ConcurrentHashMap<>())).get(field);
        if (obj == null) return null;
        return Jsons.fromJson(obj.toString(), tClass);
    }

    @Override
    public <T> Map<String, T> hgetAll(String key, Class<T> clazz) {
        Map<String, Object> m = (Map) map.get(key);
        if (m == null || m.isEmpty()) return Collections.emptyMap();

        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Object> o : m.entrySet()) {
            result.put(o.getKey(), Jsons.fromJson(String.valueOf(o.getValue()), clazz));
        }
        return result;
    }

    @Override
    public void zAdd(String key, String value) {

    }

    @Override
    public Long zCard(String key) {
        return 0L;
    }

    @Override
    public void zRem(String key, String value) {

    }

    @Override
    public <T> List<T> zrange(String key, int start, int end, Class<T> clazz) {
        return Collections.emptyList();
    }

    @Override
    public void lpush(String key, String value) {

    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) {
        return Collections.emptyList();
    }

    private void loadFormFile() {
        try {
            Path dir = Paths.get(this.getClass().getResource("/").toURI());
            Path data = Paths.get(dir.toString(), "cache.dat");
            if (Files.exists(data)) {
                byte[] bytes = Files.readAllBytes(data);
                if (bytes != null && bytes.length > 0) {
                    map = Jsons.fromJson(bytes, Map.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile() {
        try {
            Path dir = Paths.get(this.getClass().getResource("/").toURI());
            Path data = Paths.get(dir.toString(), "cache.dat");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.deleteIfExists(data);
            Files.write(data, Jsons.toJson(map).getBytes(Constants.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
