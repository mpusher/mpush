package com.mpush.api.utils;

import java.util.*;

/**
 * @description:
 * @author: dengliaoyan
 * @create: 2019-06-13 18:33
 **/
public class SetUtil {
    public static <T> Set<T> toSet(T t){
        return new HashSet<>(Arrays.asList(t));
    }
    public static <T> Set<T> toSet(T[] t){
        return new HashSet<>(Arrays.asList(t));
    }
    public static <T> Set<T> toSet(List<T> t){
        return new HashSet<>(t);
    }
    public static <T, K> Set<T> toSet(Map<K,T> t){
        return new HashSet<>(t.values());
    }
}
