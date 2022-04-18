package io.graphine.core.util;

import java.util.StringJoiner;

/**
 * @author Oleg Marchenko
 */
public final class WildcardParameterRepeater {
    private static final String PARAMETER_SEPARATOR = ", ";
    private static final String PARAMETER_WILDCARD = "?";

    public static String repeat(int count) {
        StringJoiner joiner = new StringJoiner(PARAMETER_SEPARATOR);
        for (int i = 1; i <= count; i++) {
            joiner.add(PARAMETER_WILDCARD);
        }
        return joiner.toString();
    }

    public static String repeatFor(Iterable<?> elements) {
        StringJoiner joiner = new StringJoiner(PARAMETER_SEPARATOR);
        for (Object element : elements) {
            joiner.add(PARAMETER_WILDCARD);
        }
        return joiner.toString();
    }

    private WildcardParameterRepeater() {
        throw new UnsupportedOperationException();
    }
}
