package io.graphine.processor.util;

import io.graphine.core.annotation.Repository;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Map.Entry;

import static io.graphine.processor.support.EnvironmentContext.elementUtils;

/**
 * @author Oleg Marchenko
 */
public final class RepositoryAnnotationUtils {

    public static TypeElement getEntityElementFromRepositoryAnnotation(TypeElement targetElement) {
        AnnotationValue annotationValue = getRepositoryAnnotationValue(targetElement);
        return elementUtils.getTypeElement(annotationValue.getValue().toString());
    }

    public static AnnotationMirror getRepositoryAnnotation(TypeElement targetElement) {
        String annotationName = Repository.class.getName();
        for (AnnotationMirror annotationMirror : targetElement.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
                return annotationMirror;
            }
        }

        // Should be an unreachable exception for an element with @Repository annotation.
        throw new IllegalArgumentException("Target element '" + targetElement.getQualifiedName() +
                                                   "' does not contain Repository annotation");
    }

    public static AnnotationValue getRepositoryAnnotationValue(TypeElement targetElement) {
        AnnotationMirror annotation = getRepositoryAnnotation(targetElement);
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if ("value".equals(entry.getKey().getSimpleName().toString())) {
                return entry.getValue();
            }
        }

        // Unreachable exception for @Repository annotation.
        throw new IllegalArgumentException("Repository annotation does not contain property value");
    }

    private RepositoryAnnotationUtils() {
    }
}
