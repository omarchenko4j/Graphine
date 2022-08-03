package io.graphine.processor.metadata.factory.entity;

import io.graphine.processor.metadata.model.entity.attribute.AttributeMapperMetadata;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.elementUtils;
import static io.graphine.processor.support.EnvironmentContext.typeUtils;
import static io.graphine.processor.util.GraphineAnnotationUtils.getAttributeMapperAnnotationValueAttributeValue;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * @author Oleg Marchenko
 */
public final class AttributeMapperMetadataFactory {
    public AttributeMapperMetadata create(TypeElement attributeMapperElement) {
        TypeElement attributeTypeElement =
                getAttributeMapperAnnotationValueAttributeValue(attributeMapperElement);

        List<ExecutableElement> methods = methodsIn(attributeMapperElement.getEnclosedElements());
        String getterMethodName = findGetterMethodName(methods, attributeTypeElement);
        String setterMethodName = findSetterMethodName(methods, attributeTypeElement);

        String attributeTypeQualifiedName = attributeTypeElement.getQualifiedName().toString();
        return new AttributeMapperMetadata(attributeMapperElement,
                                           attributeTypeQualifiedName,
                                           getterMethodName,
                                           setterMethodName);
    }

    private String findGetterMethodName(List<ExecutableElement> methods, TypeElement attributeTypeElement) {
        for (ExecutableElement method : methods) {
            Set<Modifier> methodModifiers = method.getModifiers();
            if (!methodModifiers.contains(PUBLIC)) {
                continue;
            }
            if (!methodModifiers.contains(STATIC)) {
                continue;
            }

            if (!typeUtils.isSameType(method.getReturnType(), attributeTypeElement.asType())) {
                continue;
            }

            List<? extends VariableElement> methodParameters = method.getParameters();
            if (methodParameters.size() != 2) {
                continue;
            }
            TypeElement resultSetElement = elementUtils.getTypeElement("java.sql.ResultSet");
            if (!typeUtils.isSameType(methodParameters.get(0).asType(), resultSetElement.asType())) {
                continue;
            }
            if (methodParameters.get(1).asType().getKind() != TypeKind.INT) {
                continue;
            }

            return method.getSimpleName().toString();
        }

        throw new IllegalStateException();
    }

    private String findSetterMethodName(List<ExecutableElement> methods, TypeElement attributeTypeElement) {
        for (ExecutableElement method : methods) {
            Set<Modifier> methodModifiers = method.getModifiers();
            if (!methodModifiers.contains(PUBLIC)) {
                continue;
            }
            if (!methodModifiers.contains(STATIC)) {
                continue;
            }

            List<? extends VariableElement> methodParameters = method.getParameters();
            if (methodParameters.size() != 3) {
                continue;
            }
            TypeElement preparedStatementElement = elementUtils.getTypeElement("java.sql.PreparedStatement");
            if (!typeUtils.isSameType(methodParameters.get(0).asType(), preparedStatementElement.asType())) {
                continue;
            }
            if (methodParameters.get(1).asType().getKind() != TypeKind.INT) {
                continue;
            }
            if (!typeUtils.isSameType(methodParameters.get(2).asType(), attributeTypeElement.asType())) {
                continue;
            }

            return method.getSimpleName().toString();
        }

        throw new IllegalStateException();
    }
}
