package io.graphine.processor.metadata.model.repository.method;

import io.graphine.processor.metadata.model.NativeElementMetadata;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Oleg Marchenko
 */
public class MethodMetadata extends NativeElementMetadata<ExecutableElement> {
    public MethodMetadata(ExecutableElement element) {
        super(element);
    }

    @Override
    public String toString() {
        return nativeElement.toString();
    }
}
