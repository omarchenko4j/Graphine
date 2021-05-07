package io.graphine.processor.util;

import static java.util.Objects.isNull;

/**
 * @author Oleg Marchenko
 */
public final class EnumUtils {

    public static <E extends Enum<E>> E valueOf(Class<E> enumType, String name) {
        try {
            return Enum.valueOf(enumType, name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <E extends Enum<E>> E valueOf(String name, E defaultValue) {
        E value = valueOf(defaultValue.getDeclaringClass(), name);
        if (isNull(value)) {
            return defaultValue;
        }
        return value;
    }

    private EnumUtils() {
    }
}
