package io.graphine.processor.query.model.parameter;

/**
 * @author Oleg Marchenko
 */
public interface ParameterVisitor<R> {
    R visit(Parameter parameter);
    R visit(ComplexParameter parameter);
    // TODO: remove default modifier after implement it
    default R visit(IterableParameter parameter) {
        return null;
    }
}
