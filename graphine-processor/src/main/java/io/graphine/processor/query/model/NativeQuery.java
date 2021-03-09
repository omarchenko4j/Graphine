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
    private final List<Parameter> consumedParameters;

    public NativeQuery(String value,
                       List<Parameter> producedParameters,
                       List<Parameter> consumedParameters) {
        this.value = value;
        this.producedParameters = producedParameters;
        this.consumedParameters = consumedParameters;
    }

    public String getValue() {
        return value;
    }

    public List<Parameter> getProducedParameters() {
        return unmodifiableList(producedParameters);
    }

    public List<Parameter> getConsumedParameters() {
        return unmodifiableList(consumedParameters);
    }

    @Override
    public String toString() {
        return value;
    }
}
