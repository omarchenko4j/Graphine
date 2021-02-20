package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.core.annotation.Attribute;
import io.graphine.core.annotation.Id;
import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.VariableElement;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class AttributeMetadata extends NativeElement<VariableElement> {
    protected final String column;

    public AttributeMetadata(VariableElement element, String column) {
        super(element);
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return name + " [" + column + ']';
    }

    public static boolean isAttribute(VariableElement fieldElement) {
        return nonNull(fieldElement.getAnnotation(Id.class)) ||
               nonNull(fieldElement.getAnnotation(Attribute.class));
    }
}
