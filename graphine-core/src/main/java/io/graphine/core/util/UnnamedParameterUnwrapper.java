package io.graphine.core.util;

import java.util.StringJoiner;

/**
 * @author Oleg Marchenko
 */
public final class UnnamedParameterUnwrapper {
    public static <T> String unwrapFor(T... elements) {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 1; i <= elements.length; i++) {
            joiner.add("?");
        }
        return joiner.toString();
    }

    public static <T> String unwrapFor(Iterable<T> elements) {
        StringJoiner joiner = new StringJoiner(", ");
        elements.forEach(element -> joiner.add("?"));
        return joiner.toString();
    }

    private UnnamedParameterUnwrapper() {
    }
}
