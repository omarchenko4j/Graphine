package io.graphine.processor.util;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static io.graphine.processor.util.StringUtils.capitalize;
import static javax.lang.model.type.TypeKind.BOOLEAN;

/**
 * @author Oleg Marchenko
 */
public final class MethodUtils {
    private static final String GETTER_PREFIX = "get";
    private static final String BOOLEAN_GETTER_PREFIX = "is";
    private static final String SETTER_PREFIX = "set";

    public static String getter(VariableElement field) {
        TypeMirror fieldType = field.asType();
        String fieldName = field.getSimpleName().toString();
        return getter(fieldType, fieldName);
    }

    public static String getter(TypeMirror fieldType, String fieldName) {
        if (fieldType.getKind() == BOOLEAN) {
            return methodNameWithPrefix(fieldName, BOOLEAN_GETTER_PREFIX);
        }
        return methodNameWithPrefix(fieldName, GETTER_PREFIX);
    }

    public static String setter(VariableElement field) {
        String fieldName = field.getSimpleName().toString();
        return setter(fieldName);
    }

    public static String setter(String fieldName) {
        return methodNameWithPrefix(fieldName, SETTER_PREFIX);
    }

    private static String methodNameWithPrefix(String fieldName, String prefix) {
        return prefix + capitalize(fieldName);
    }

    private MethodUtils() {
    }
}
