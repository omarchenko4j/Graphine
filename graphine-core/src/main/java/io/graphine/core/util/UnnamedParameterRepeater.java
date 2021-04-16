package io.graphine.core.util;

import java.util.StringJoiner;

/**
 * @author Oleg Marchenko
 */
public final class UnnamedParameterRepeater {
    public static String repeat(int count) {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 1; i <= count; i++) {
            joiner.add("?");
        }
        return joiner.toString();
    }

    public static String repeatFor(Iterable<?> elements) {
        StringJoiner joiner = new StringJoiner(", ");
        elements.forEach(element -> joiner.add("?"));
        return joiner.toString();
    }

    private UnnamedParameterRepeater() {
    }
}
