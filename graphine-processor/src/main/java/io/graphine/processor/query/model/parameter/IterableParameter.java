package io.graphine.processor.query.model.parameter;

/**
 * @author Oleg Marchenko
 */
public class IterableParameter extends Parameter {
    private final Parameter iteratedParameter;

    public IterableParameter(Parameter parameter, Parameter iteratedParameter) {
        super(parameter);
        this.iteratedParameter = iteratedParameter;
    }

    public Parameter getIteratedParameter() {
        return iteratedParameter;
    }

    @Override
    public <R> R accept(ParameterVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return iteratedParameter.toString() + " of " + super.toString();
    }
}
