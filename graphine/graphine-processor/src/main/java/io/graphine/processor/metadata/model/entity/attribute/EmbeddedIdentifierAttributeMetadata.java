package io.graphine.processor.metadata.model.entity.attribute;

import javax.lang.model.element.VariableElement;

import static io.graphine.processor.metadata.model.entity.attribute.EmbeddedAttributeMetadata.isEmbedded;

/**
 * @author Oleg Marchenko
 */
public class EmbeddedIdentifierAttributeMetadata extends IdentifierAttributeMetadata {
    private final EmbeddedAttributeMetadata embeddedAttribute;

    public EmbeddedIdentifierAttributeMetadata(VariableElement element,
                                               EmbeddedAttributeMetadata embeddedAttribute) {
        super(element, null);
        this.embeddedAttribute = embeddedAttribute;
    }

    public EmbeddedAttributeMetadata getEmbeddedAttribute() {
        return embeddedAttribute;
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean isEmbeddedIdentifier(VariableElement fieldElement) {
        return isIdentifier(fieldElement) && isEmbedded(fieldElement);
    }
}
