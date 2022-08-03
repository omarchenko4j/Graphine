package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;

/**
 * @author Oleg Marchenko
 */
public class AttributeMapperMetadata extends NativeTypeElement {
    private final String forAttributeType;
    private final String getterMethodName;
    private final String setterMethodName;

    public AttributeMapperMetadata(TypeElement element,
                                   String forAttributeType,
                                   String getterMethodName,
                                   String setterMethodName) {
        super(element);
        this.forAttributeType = forAttributeType;
        this.getterMethodName = getterMethodName;
        this.setterMethodName = setterMethodName;
    }

    public String getForAttributeType() {
        return forAttributeType;
    }

    public String getGetterMethodName() {
        return getterMethodName;
    }

    public String getSetterMethodName() {
        return setterMethodName;
    }
}
