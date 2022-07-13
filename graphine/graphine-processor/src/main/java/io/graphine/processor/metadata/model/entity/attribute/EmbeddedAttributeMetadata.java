package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.annotation.Embeddable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class EmbeddedAttributeMetadata extends AttributeMetadata {
    private final Map<String, String> attributeNameToColumnNameMap;

    public EmbeddedAttributeMetadata(VariableElement element,
                                     Map<String, String> attributeNameToColumnNameMap) {
        super(element, null);
        this.attributeNameToColumnNameMap = attributeNameToColumnNameMap;
    }

    public String overrideAttribute(AttributeMetadata attribute) {
        return attributeNameToColumnNameMap.getOrDefault(attribute.getName(), attribute.getColumn());
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
