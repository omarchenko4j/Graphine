package io.graphine.processor.util;

import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import static io.graphine.processor.support.EnvironmentContext.elementUtils;

/**
 * @author Oleg Marchenko
 */
public final class AnnotationUtils {

    public static TypeElement retrieveClassProperty(Element targetElement,
                                                    Class<? extends Annotation> annotationClass,
                                                    String annotationProperty) {
        String annotationName = annotationClass.getName();
        for (AnnotationMirror annotationMirror : targetElement.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                    if (annotationProperty.equals(entry.getKey().getSimpleName().toString())) {
                        AnnotationValue annotationValue = entry.getValue();
                        return elementUtils.getTypeElement(annotationValue.getValue().toString());
                    }
                }
            }
        }
        throw new IllegalArgumentException("Target element '" + targetElement.getSimpleName() +
                                           "' does not contain specified annotation '" + annotationName +
                                           "' or annotation property '" + annotationProperty + "'");
    }

    private AnnotationUtils() {
    }
}
