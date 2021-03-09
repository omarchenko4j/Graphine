package io.graphine.processor.query.model.parameter;

import java.util.List;

import static io.graphine.processor.util.StringUtils.join;
import static java.util.Collections.unmodifiableList;

/**
 * @author Oleg Marchenko
 */
public class ComplexParameter extends Parameter {
    private final List<Parameter> childParameters;

    public ComplexParameter(Parameter parentParameter, List<Parameter> childParameters) {
        super(parentParameter);
        this.childParameters = childParameters;
    }

    public List<Parameter> getChildParameters() {
        return unmodifiableList(childParameters);
    }

    @Override
    public String toString() {
        return super.toString() + " " + join(childParameters, ", ", "{", "}");
    }
}
