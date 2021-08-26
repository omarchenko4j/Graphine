package io.graphine.processor.query.model;

import io.graphine.processor.query.model.parameter.Parameter;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class NativeQuery {
    private final String value;
    private final List<Parameter> producedParameters;

    public NativeQuery(String value,
                       List<Parameter> producedParameters) {
        this.value = value;
        this.producedParameters = producedParameters;
    }

    public String getValue() {
        return value;
    }

    public List<Parameter> getProducedParameters() {
        return unmodifiableList(producedParameters);
    }

    @Override
    public String toString() {
        return value;
    }
}
