package dev.w0fv1.vaadmin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import java.time.OffsetDateTime;
import java.util.*;

public class TypeUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static <T> T convert(String input, Class<T> targetType, Class<?> subType) {
        if (input == null) return null;

        try {
            if (List.class.isAssignableFrom(targetType) && subType != null) {
                CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, subType);
                return objectMapper.readValue(input, listType);
            } else if (Set.class.isAssignableFrom(targetType) && subType != null) {
                CollectionType setType = objectMapper.getTypeFactory().constructCollectionType(Set.class, subType);
                return objectMapper.readValue(input, setType);
            }
            return objectMapper.convertValue(input, targetType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
        } else if (clazz == List.class) {
            return (T) new ArrayList();
        } else if (clazz == Set.class) {
            return (T) new HashSet();
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

    /**
     * 判断对象是否为"空"
     *
     * 支持检查：
     * - null
     * - 字符串：长度为0或全空白
     * - 集合（List, Set）：为空
     * - Map：为空
     * - 数组：长度为0
     *
     * @param obj 输入对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Arrays.asList((Object[]) obj).isEmpty();
        }
        return false;
    }
}
