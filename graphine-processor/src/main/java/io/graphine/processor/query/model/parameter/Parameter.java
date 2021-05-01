package io.graphine.processor.query.model.parameter;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Oleg Marchenko
 */
public class Parameter {
    private final String name;
    private final TypeMirror type;

    public Parameter(String name, TypeMirror type) {
        this.name = name;
        this.type = type;
    }

    protected Parameter(Parameter parameter) {
        this(parameter.name, parameter.type);
    }

    public String getName() {
        return name;
    }

    public TypeMirror getType() {
        return type;
    }

    public <R> R accept(ParameterVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Parameter basedOn(VariableElement element) {
        String parameterName = element.getSimpleName().toString();
        TypeMirror parameterType = element.asType();
        return new Parameter(parameterName, parameterType);
    }
}
