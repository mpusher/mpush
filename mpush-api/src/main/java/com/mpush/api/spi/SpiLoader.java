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
