package io.graphine.processor.metadata.validator.entity;

import io.graphine.annotation.AttributeMapper;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static io.graphine.processor.support.EnvironmentContext.*;
import static io.graphine.processor.util.AnnotationUtils.findAnnotation;
import static io.graphine.processor.util.AnnotationUtils.findAnnotationValue;
import static io.graphine.processor.util.GraphineAnnotationUtils.getAttributeMapperAnnotationValueAttributeValue;
import static java.util.Objects.isNull;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * @author Oleg Marchenko
 */
public final class AttributeMapperElementValidator {
    public boolean validate(TypeElement attributeMapperElement) {
        boolean valid = true;

        if (attributeMapperElement.getKind() != CLASS) {
            valid = false;
            logger.error("Attribute mapper must be a class", attributeMapperElement);
        }
        if (!attributeMapperElement.getModifiers().contains(PUBLIC)) {
            valid = false;
            logger.error("Attribute mapper class must be public", attributeMapperElement);
        }

        TypeElement attributeTypeElement =
                getAttributeMapperAnnotationValueAttributeValue(attributeMapperElement);
        if (!attributeTypeElement.getTypeParameters().isEmpty()) {
            String annotationName = AttributeMapper.class.getName();
            AnnotationMirror annotation = findAnnotation(attributeTypeElement, annotationName).get();
            AnnotationValue annotationValue = findAnnotationValue(annotation, "value").get();
            logger.error("Parameterized attribute type not supported",
                         attributeMapperElement, annotation, annotationValue);
            return false;
        }

        List<ExecutableElement> methods = methodsIn(attributeMapperElement.getEnclosedElements());

        ExecutableElement getterMethod = findGetterMethodBySignature(methods);
        if (isNull(getterMethod)) {
            valid = false;
            logger.error(String.format("Attribute mapper class must have a method with signature: " +
                                               "%s get...(ResultSet resultSet, int columnIndex) throws SQLException",
                                       attributeTypeElement.getSimpleName()),
                         attributeMapperElement);
        }
        else {
            if (!typeUtils.isSameType(getterMethod.getReturnType(), attributeTypeElement.asType())) {
                valid = false;
                logger.error("Method must return '" + attributeTypeElement.getSimpleName() + "' type", getterMethod);
            }

            Set<Modifier> methodModifiers = getterMethod.getModifiers();
            if (!methodModifiers.contains(PUBLIC)) {
                valid = false;
                logger.error("Method must be public", getterMethod);
            }
            if (!methodModifiers.contains(STATIC)) {
                valid = false;
                logger.error("Method must be static", getterMethod);
            }

            List<? extends TypeMirror> methodThrownTypes = getterMethod.getThrownTypes();
            if (methodThrownTypes.isEmpty()) {
                logger.mandatoryWarn("Do not handle SQL exceptions manually. Throw them outside: throws SQLException",
                                     getterMethod);
            }
            else if (methodThrownTypes.size() > 1) {
                valid = false;
                logger.error("Throwing only SQL exceptions is supported. Use: throws SQLException", getterMethod);
            }
            else {
                TypeMirror thrownType = methodThrownTypes.get(0);
                TypeElement sqlExceptionElement = elementUtils.getTypeElement("java.sql.SQLException");
                if (!typeUtils.isSameType(thrownType, sqlExceptionElement.asType())) {
                    valid = false;
                    logger.error("Throwing only SQL exceptions is supported. Use: throws SQLException", getterMethod);
                }
            }
        }

        ExecutableElement setterMethod = findSetterMethodBySignature(methods, attributeTypeElement);
        if (isNull(setterMethod)) {
            valid = false;
            logger.error(String.format("Attribute mapper class must have a method with signature: " +
                                               "void set...(PreparedStatement statement, int columnIndex, %s value) throws SQLException",
                                       attributeTypeElement.getSimpleName()),
                         attributeMapperElement);
        }
        else {
            Set<Modifier> methodModifiers = setterMethod.getModifiers();
            if (!methodModifiers.contains(PUBLIC)) {
                valid = false;
                logger.error("Method must be public", setterMethod);
            }
            if (!methodModifiers.contains(STATIC)) {
                valid = false;
                logger.error("Method must be static", setterMethod);
            }

            List<? extends TypeMirror> methodThrownTypes = setterMethod.getThrownTypes();
            if (methodThrownTypes.isEmpty()) {
                logger.mandatoryWarn("Do not handle SQL exceptions manually. Throw them outside: throws SQLException",
                                     setterMethod);
            }
            else if (methodThrownTypes.size() > 1) {
                valid = false;
                logger.error("Throwing only SQL exceptions is supported. Use: throws SQLException", setterMethod);
            }
            else {
                TypeMirror thrownType = methodThrownTypes.get(0);
                TypeElement sqlExceptionElement = elementUtils.getTypeElement("java.sql.SQLException");
                if (!typeUtils.isSameType(thrownType, sqlExceptionElement.asType())) {
                    valid = false;
                    logger.error("Throwing only SQL exceptions is supported. Use: throws SQLException", setterMethod);
                }
            }
        }

        return valid;
    }

    private ExecutableElement findGetterMethodBySignature(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
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
            return method;
        }
        return null;
    }

    private ExecutableElement findSetterMethodBySignature(List<ExecutableElement> methods,
                                                          TypeElement attributeTypeElement) {
        for (ExecutableElement method : methods) {
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
            return method;
        }
        return null;
    }
}
