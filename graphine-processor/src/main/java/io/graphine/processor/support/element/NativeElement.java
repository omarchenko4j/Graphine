package io.graphine.processor.support.element;

import javax.lang.model.element.Element;

/**
 * @author Oleg Marchenko
 */
public class NativeElement<E extends Element> {
    protected final E nativeElement;
    protected final String name;

    protected NativeElement(E element) {
        this.nativeElement = element;
        this.name = element.getSimpleName().toString();
    }

    public E getNativeElement() {
        return nativeElement;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
