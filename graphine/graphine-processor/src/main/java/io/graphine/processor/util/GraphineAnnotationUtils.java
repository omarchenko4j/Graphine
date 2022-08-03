package io.graphine.processor.util;

import io.graphine.annotation.Attribute;
import io.graphine.annotation.AttributeMapper;
import io.graphine.annotation.Repository;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import static io.graphine.processor.support.EnvironmentContext.elementUtils;
import static io.graphine.processor.util.AnnotationUtils.findAnnotation;
import static io.graphine.processor.util.AnnotationUtils.findAnnotationValue;

/**
 * @author Oleg Marchenko
 */
public final class GraphineAnnotationUtils {

    public static TypeElement getRepositoryAnnotationValueAttributeValue(TypeElement targetElement) {
        return findAnnotation(targetElement, Repository.class.getName())
                .flatMap(annotation -> findAnnotationValue(annotation, "value"))
                .map(annotationValue -> elementUtils.getTypeElement(annotationValue.getValue().toString()))
                .orElseThrow(() -> {
                    String message =
                            String.format("Target element '%s' does not contain @%s annotation",
                                          targetElement.getQualifiedName(),
                                          Repository.class.getName());
                    return new IllegalArgumentException(message);
                });
    }

    public static TypeElement getAttributeMapperAnnotationValueAttributeValue(TypeElement targetElement) {
        return findAnnotation(targetElement, AttributeMapper.class.getName())
                .flatMap(annotation -> findAnnotationValue(annotation, "value"))
                .map(annotationValue -> elementUtils.getTypeElement(annotationValue.getValue().toString()))
                .orElseThrow(() -> {
                    String message =
                            String.format("Target element '%s' does not contain @%s annotation",
                                          targetElement.getQualifiedName(),
                                          AttributeMapper.class.getName());
                    return new IllegalArgumentException(message);
                });
    }

    public static TypeElement getAttributeAnnotationMapperAttributeValue(VariableElement targetElement) {
        return findAnnotation(targetElement, Attribute.class.getName())
                .flatMap(annotation -> findAnnotationValue(annotation, "mapper"))
                .filter(annotationValue -> annotationValue.getValue() instanceof DeclaredType)
                .map(annotationValue -> elementUtils.getTypeElement(annotationValue.getValue().toString()))
                .orElse(null);
    }

    private GraphineAnnotationUtils() {
    }
}
