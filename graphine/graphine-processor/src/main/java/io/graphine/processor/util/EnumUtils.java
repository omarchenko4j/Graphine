package io.graphine.processor.util;

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

    private EnumUtils() {
    }
}
