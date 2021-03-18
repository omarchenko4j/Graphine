package io.graphine.processor.query.model.parameter;

import java.util.function.Function;

/**
 * @author Oleg Marchenko
 */
public class ComputableParameter extends Parameter {
    private final Function<Parameter, Parameter> function;

    public ComputableParameter(Parameter parameter, Function<Parameter, Parameter> function) {
        super(parameter);
        this.function = function;
    }

    @Override
    public <R> R accept(ParameterVisitor<R> visitor) {
        return visitor.visit(function.apply(this));
    }
}
