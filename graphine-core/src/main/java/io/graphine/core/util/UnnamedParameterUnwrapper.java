package io.graphine.core.util;

import java.util.StringJoiner;

/**
 * @author Oleg Marchenko
 */
public final class UnnamedParameterUnwrapper {
    public static String unwrapFor(byte... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(short... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(int... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(long... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(float... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(double... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    public static String unwrapFor(Object... elements) {
        return repeatUnnamedParameterSeparatedByComma(elements.length);
    }

    private static String repeatUnnamedParameterSeparatedByComma(int count) {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 1; i <= count; i++) {
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
