package dev.w0fv1.vaadmin.util;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

public class TypeUtil {

    @SuppressWarnings("unchecked")
    public static <T> T defaultIfNull(Object data, Class<T> clazz) {
        if (data != null) {
            return (T) data;
        }

        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(0);
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(0L);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(0.0);
        } else if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(0.0f);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.FALSE;
        } else if (clazz == String.class) {
            return (T) "";
        } else if (clazz == Character.class || clazz == char.class) {
            return (T) Character.valueOf('\u0000');
        } else if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf((short) 0);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf((byte) 0);
        } else if (clazz == OffsetDateTime.class) {
            return (T) OffsetDateTime.now();
        }

        // 如果类型不在上述范围内，可以根据需求返回 null 或抛出异常
        return null;
    }

    private static final Set<Class<?>> BASE_TYPES = new HashSet<Class<?>>() {{
        add(Integer.class);
        add(Long.class);
        add(Double.class);
        add(Float.class);
        add(Boolean.class);
        add(Character.class);
        add(Short.class);
        add(Byte.class);
        add(String.class);
        add(OffsetDateTime.class);
    }};


    public static Boolean isBaseTye(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        if (BASE_TYPES.contains(clazz)) {
            return true;
        }
        if (clazz.isEnum()) {
            return true;
        }
        return false;
    }
}
