package io.graphine.processor.util;

import static io.graphine.processor.util.StringUtils.capitalize;

/**
 * @author Oleg Marchenko
 */
public final class MethodUtils {
    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";

    public static String getter(String fieldName) {
        return methodNameWithPrefix(fieldName, GETTER_PREFIX);
    }

    public static String setter(String fieldName) {
        return methodNameWithPrefix(fieldName, SETTER_PREFIX);
    }

    private static String methodNameWithPrefix(String fieldName, String prefix) {
        return prefix + capitalize(fieldName);
    }

    private MethodUtils() {
    }
}
