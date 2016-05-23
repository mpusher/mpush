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

package com.mpush.api.spi;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class SpiLoader {
    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    public static <T> T load(Class<T> clazz) {
        return load(clazz, null);
    }

    public static <T> T load(Class<T> clazz, String name) {
        String key = clazz.getName();
        Object o = CACHE.get(key);
        if (o == null) {
            T t = load0(clazz, name);
            if (t != null) {
                CACHE.put(key, t);
                return t;
            }
        } else if (clazz.isInstance(o)) {
            return (T) o;
        }
        return load0(clazz, name);
    }

    public static <T> T load0(Class<T> clazz, String name) {
        ServiceLoader<T> factories = ServiceLoader.load(clazz);
        T t = filterByName(factories, name);

        if (t == null) {
            factories = ServiceLoader.load(clazz, SpiLoader.class.getClassLoader());
            t = filterByName(factories, name);
        }

        if (t != null) {
            return t;
        } else {
            throw new IllegalStateException("Cannot find META-INF/services/" + clazz.getName() + " on classpath");
        }
    }

    private static <T> T filterByName(ServiceLoader<T> factories, String name) {
        Iterator<T> it = factories.iterator();
        if (name == null) {
            if (it.hasNext()) {
                return it.next();
            }
        } else {
            while (it.hasNext()) {
                T t = it.next();
                if (name.equals(t.getClass().getSimpleName())) {
                    return t;
                }
            }
        }
        return null;
    }
}
