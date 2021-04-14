package io.graphine.processor.query.model.parameter;

/**
 * @author Oleg Marchenko
 */
public interface ParameterVisitor<R> {
    R visit(Parameter parameter);
    R visit(ComplexParameter parameter);
    R visit(IterableParameter parameter);
}
