package io.graphine.processor.query.model;

/**
 * @author Oleg Marchenko
 */
public class NativeQuery {
    private final String value;

    public NativeQuery(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
