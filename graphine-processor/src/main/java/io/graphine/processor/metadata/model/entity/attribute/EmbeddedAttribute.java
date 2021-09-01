package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.core.annotation.Embeddable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class EmbeddedAttribute extends AttributeMetadata {
    public EmbeddedAttribute(VariableElement element) {
        super(element, null);
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean isEmbedded(VariableElement fieldElement) {
        TypeMirror fieldType = fieldElement.asType();
        if (fieldType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) fieldType;
            return nonNull(declaredType.asElement().getAnnotation(Embeddable.class));
        }
        return false;
    }
}
