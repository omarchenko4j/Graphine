package io.graphine.processor.util;

/**
 * @author Oleg Marchenko
 */
public final class VariableNameUniqueizer {
    private static final char PREFIX = '_';

    public static String uniqueize(String variableName) {
        return PREFIX + variableName;
    }

    private VariableNameUniqueizer() {
    }
}
