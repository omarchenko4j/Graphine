package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.annotation.Id;

import javax.lang.model.element.VariableElement;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class IdentifierAttributeMetadata extends AttributeMetadata {
    public IdentifierAttributeMetadata(VariableElement element, String column) {
        super(element, column);
    }

    public static boolean isIdentifier(VariableElement fieldElement) {
        return nonNull(fieldElement.getAnnotation(Id.class));
    }
}
