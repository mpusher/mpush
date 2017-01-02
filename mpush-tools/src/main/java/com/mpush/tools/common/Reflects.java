package com.mpush.tools.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reflects {

    public static Class getSuperClassGenericType(final Class<?> clazz, int index) {
        return getGenericType(clazz.getGenericSuperclass(), index);
    }

    public static Class getFieldGenericType(final Field field, final int index) {
        return getGenericType(field.getGenericType(), index);
    }

    public static List<Class> getMethodGenericTypes(final Method method, final int paramIndex) {
        return getGenericTypes(method.getGenericParameterTypes()[paramIndex]);
    }


    public static Class getGenericType(Type genType, int index) {
        List<Class> params = getGenericTypes(genType);
        if (index >= params.size() || index < 0) return null;
        return params.get(index);
    }

    public static List<Class> getGenericTypes(Type genType) {
        if (!(genType instanceof ParameterizedType)) return Collections.emptyList();
        Type[] types = ((ParameterizedType) genType).getActualTypeArguments();
        List<Class> list = new ArrayList<Class>(types.length);
        for (Type type : types) {
            if (type instanceof Class) list.add((Class) type);
            else if (type instanceof ParameterizedType) {
                Type type1 = ((ParameterizedType) type).getRawType();
                if (type1 instanceof Class) list.add((Class) type1);
            }
        }
        return list;
    }
}