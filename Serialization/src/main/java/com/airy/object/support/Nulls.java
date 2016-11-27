package com.airy.object.support;

import java.util.HashMap;
import java.util.Map;

public class Nulls {

    private static Map<Class<?>, Object> nulls = new HashMap<>();

    static {
        nulls.put(byte.class, (byte) 0);
        nulls.put(char.class, '0');
        nulls.put(short.class, (short) 0);
        nulls.put(int.class, 0);
        nulls.put(long.class, 0L);
        nulls.put(float.class, 0.0F);
        nulls.put(double.class, 0.0);
        nulls.put(boolean.class, false);
    }

    private Nulls() {
    }

    public static boolean isNull(Object obj) {
        return obj == null || obj.equals(obj.getClass());
    }

    public static boolean isNull(Object obj, Class<?> type) {
        return obj == null || obj.equals(get(type));
    }

    public static Object get(Class<?> type) {
        return nulls.get(type);
    }
}
