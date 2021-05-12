package io.graphine.processor.metadata.model;

import io.graphine.processor.support.element.NativeTypeElement;

import javax.lang.model.element.TypeElement;

/**
 * @author Oleg Marchenko
 */
public abstract class BasicMetadata extends NativeTypeElement {
    protected BasicMetadata(TypeElement element) {
        super(element);
    }
}
