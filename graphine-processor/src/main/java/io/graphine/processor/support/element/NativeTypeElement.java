package io.graphine.processor.support.element;

import javax.lang.model.element.TypeElement;

/**
 * @author Oleg Marchenko
 */
public class NativeTypeElement extends NativeElement<TypeElement> {
    protected final String qualifiedName;

    protected NativeTypeElement(TypeElement element) {
        super(element);
        this.qualifiedName = element.getQualifiedName().toString();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
