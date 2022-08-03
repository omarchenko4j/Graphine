package io.graphine.processor.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.util.Optional;

/**
 * @author Oleg Marchenko
 */
public final class AnnotationUtils {

    public static Optional<AnnotationMirror> findAnnotation(Element element, String annotationName) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
                return Optional.of(annotationMirror);
            }
        }
        return Optional.empty();
    }

    public static Optional<AnnotationValue> findAnnotationValue(AnnotationMirror annotation, String attributeName) {
        return annotation.getElementValues().entrySet()
                .stream()
                .filter(entry -> attributeName.equals(entry.getKey().getSimpleName().toString()))
                .map(entry -> (AnnotationValue) entry.getValue())
                .findFirst();
    }

    private AnnotationUtils() {
    }
}
