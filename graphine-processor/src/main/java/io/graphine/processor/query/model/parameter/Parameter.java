package io.graphine.processor.query.model.parameter;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMetadata;
import io.graphine.processor.metadata.model.repository.method.parameter.ParameterMetadata;

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

    public static Parameter basedOn(AttributeMetadata attribute) {
        String attributeName = attribute.getName();
        TypeMirror attributeType = attribute.getNativeType();
        return new Parameter(attributeName, attributeType);
    }

    public static Parameter basedOn(ParameterMetadata parameter) {
        String parameterName = parameter.getName();
        TypeMirror parameterType = parameter.getNativeType();
        return new Parameter(parameterName, parameterType);
    }
}
