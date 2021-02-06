package io.graphine.processor.metadata.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author Oleg Marchenko
 */
public abstract class NativeElementMetadata<E extends Element> {
    protected final E nativeElement;
    protected final String qualifiedName;
    protected final String name;

    protected NativeElementMetadata(E element) {
        this.nativeElement = element;
        this.qualifiedName = getQualifiedName(element);
        this.name = element.getSimpleName().toString();
    }

    public E getNativeElement() {
        return nativeElement;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    private static String getQualifiedName(Element element) {
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName().toString();
        }
        return element.getSimpleName().toString();
    }
}
