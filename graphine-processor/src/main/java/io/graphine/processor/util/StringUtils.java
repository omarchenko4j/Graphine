package io.graphine.processor.util;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Supplier;

/**
 * @author Oleg Marchenko
 */
public final class StringUtils {
    public static final String EMPTY = "";

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static String join(Collection<?> collection, String delimiter, String prefix, String suffix) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (Object element : collection) {
            joiner.add(element.toString());
        }
        return joiner.toString();
    }

    public static String repeat(String str, String delimiter, String prefix, String suffix, int count) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (int i = 1; i < count; i++) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    public static String nullToEmpty(String str) {
        return str == null ? EMPTY : str;
    }

    public static String getIfNotEmpty(String str, Supplier<String> defaultSupplier) {
        return isNotEmpty(str) ? defaultSupplier.get() : str;
    }

    private StringUtils() {
    }
}
