package io.graphine.processor.metadata;

import javax.lang.model.element.VariableElement;

/**
 * @author Oleg Marchenko
 */
public class AttributeMetadata extends NativeElementMetadata<VariableElement> {
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
}
