package io.graphine.processor.metadata.model.entity.attribute;

import io.graphine.annotation.Attribute;
import io.graphine.annotation.Embeddable;
import io.graphine.annotation.Id;
import io.graphine.processor.support.element.NativeElement;

import javax.lang.model.element.VariableElement;

import static java.util.Objects.nonNull;

/**
 * @author Oleg Marchenko
 */
public class AttributeMetadata extends NativeElement<VariableElement> {
    protected final String column;
    protected final String mapper;

    public AttributeMetadata(VariableElement element, String column, String mapper) {
        super(element);
        this.column = column;
        this.mapper = mapper;
    }

    public String getColumn() {
        return column;
    }

    public String getMapper() {
        return mapper;
    }

    @Override
    public String toString() {
        return name + " [" + column + ']';
    }

    public static boolean isAttribute(VariableElement fieldElement) {
        return nonNull(fieldElement.getAnnotation(Id.class)) ||
               nonNull(fieldElement.getAnnotation(Attribute.class)) ||
               nonNull(fieldElement.getAnnotation(Embeddable.class));
    }
}
