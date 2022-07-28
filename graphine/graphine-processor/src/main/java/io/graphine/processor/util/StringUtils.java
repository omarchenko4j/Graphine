package io.graphine.processor.util;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static java.lang.Character.*;

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
        char firstLetter = str.charAt(0);
        if (isUpperCase(firstLetter)) {
            return str;
        }
        return toUpperCase(firstLetter) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        char firstLetter = str.charAt(0);
        if (isLowerCase(firstLetter)) {
            return str;
        }
        return toLowerCase(firstLetter) + str.substring(1);
    }

    public static String join(Object[] elements, String delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object element : elements) {
            joiner.add(element.toString());
        }
        return joiner.toString();
    }

    public static String join(Collection<?> elements, String delimiter, String prefix, String suffix) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (Object element : elements) {
            joiner.add(element.toString());
        }
        return joiner.toString();
    }

    public static String repeat(String str, String delimiter, String prefix, String suffix, int count) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (int i = 1; i <= count; i++) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    public static String getIfNotEmpty(String str, Supplier<String> supplier) {
        return isNotEmpty(str) ? supplier.get() : EMPTY;
    }

    private StringUtils() {
    }
}
