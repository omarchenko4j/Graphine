package io.graphine.processor.support.element;

import javax.lang.model.element.Element;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NativeElement<?> that = (NativeElement<?>) o;
        return nativeElement.equals(that.nativeElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nativeElement);
    }

    @Override
    public String toString() {
        return name;
    }
}
